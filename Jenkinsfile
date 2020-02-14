properties([
  buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '3', daysToKeepStr: '', numToKeepStr: '20')),
  disableConcurrentBuilds(),
  parameters([
    string(name: 'IMAGE_TAG', defaultValue: 'dev', description: 'Docker image tag used when publishing'),
    string(name: 'IMAGE_NAME', defaultValue: '', description: 'Docker image name used when publishing'),
    string(name: 'DOCKER_REGISTRY', defaultValue: '', description: 'The place where docker images are published.'),
    text(name: 'PROJECT_YAML', defaultValue: '', description: 'Override the project vars used to generate documentation')
  ])
])

podTemplate(
  containers: [
//     containerTemplate(
//       name: 'git',
//       image: 'alpine/git:latest',
//       ttyEnabled: true,
//       command: 'cat',
//       envVars: [
//           envVar(key: 'HOME', value: '/root')
//         ]
//     ),
    containerTemplate(
      name: 'docker',
      image: 'docker:latest',
      ttyEnabled: true,
      command: 'cat',
      privileged: true
    ),
    containerTemplate(
      envVars: [
        envVar(key: 'JENKINS_CERT_FILE', value: '/secrets/cert.pem')
      ],
      image: "${DOCKER_REGISTRY}/jnlp-agent:latest",
      name: 'jnlp', // using Jenkins agent image
      ttyEnabled: true,
    ),
    containerTemplate(
        name: 'ktis-doc-builder',
        image: "${DOCKER_REGISTRY}/ktis-doc-builder:latest",
        command: 'cat',
        ttyEnabled: true,
        envVars: [
          envVar(key: 'HOME', value: '/root')
        ]
    )
  ],
  volumes: [
    secretVolume(
      mountPath: '/secrets',
      secretName: 'ca-cert'
    ),
    hostPathVolume(
      hostPath: '/var/run/docker.sock',
      mountPath: '/var/run/docker.sock'
    ),
    secretVolume(
      mountPath: '/bitbucket_secret',
      secretName: 'ktis-bitbucket-ssh-private-key',
      defaultMode: '384'
    )
  ]
) {
  node(POD_LABEL) {
    stage('Config SSH') {
      container('ktis-doc-builder') {
        sh '''
          mkdir /root/.ssh
          cp -H /bitbucket_secret/ssh-privatekey /root/.ssh/id_rsa
          echo "Host *" > /root/.ssh/config
          echo "    StrictHostKeyChecking=no" >> /root/.ssh/config
        '''
      }
    }

    stage('Clone Repos') {
        container('ktis-doc-builder') {
            sh '''
              cd /mkdocs-site
              echo "${PROJECT_YAML}" > local_vars.yml
              python3 tasks/clone_repos.py -c local_vars.yml
            '''
        }
    }

    stage('Build site') {
      container('ktis-doc-builder') {
      sh '''
        cd /mkdocs-site
        python3 tasks/generate.py -c local_vars.yml
      '''
      }
    }

    stage('Build Service') {
      container('docker') {
        sh '''
          cd /mkdocs-site
          mv site/ docker/docs-service/site/
          docker build docker/docs-service/ -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
          docker publish ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
        '''
      }
    }
  }
}
