properties([
  buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '3', daysToKeepStr: '', numToKeepStr: '20')),
  disableConcurrentBuilds(),
  parameters([
    string(name: 'IMAGE_TAG', defaultValue: 'latest', description: 'Docker image tag used when publishing'),
    string(name: 'IMAGE_NAME', defaultValue: '', description: 'Docker image name used when publishing'),
    string(name: 'DOCKER_REGISTRY', defaultValue: '', description: 'The place where docker images are published.'),
    text(name: 'ADHOC_PROJECT_YAML', defaultValue: '', description: 'Override the project vars used to generate documentation')
  ])
])

podTemplate(
  containers: [
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
          if (ADHOC_PROJECT_YAML == '') {
            checkout(scm)
            sh 'cp ./ktis_vars.yml /mkdocs-site/local_vars.yml'
            
          } else {
            sh 'echo "${ADHOC_PROJECT_YAML}" > /mkdocs-site/local_vars.yml'
          }
          sh '''
            cd /mkdocs-site
            python3 tasks/clone_repos.py -c local_vars.yml
          '''
        }
    }

    stage('Build site') {
      container('ktis-doc-builder') {
      sh '''
        cd /mkdocs-site
        python3 tasks/generate.py -c local_vars.yml
        cp -r site/ /home/jenkins/agent/site/
        cp docker/docs-service/Dockerfile /home/jenkins/agent/Dockerfile
      '''
      }
    }

    stage('Build Service') {
      container('docker') {
        withDockerRegistry(credentialsId: 'nexus-credentials', url: "https://${DOCKER_REGISTRY}") {
          sh '''
            cd /home/jenkins/agent
            docker build . -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
            docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
          '''
        }
      }
    }
  }
}
