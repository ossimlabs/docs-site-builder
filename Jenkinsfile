properties([
  buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '3', daysToKeepStr: '', numToKeepStr: '20')),
  disableConcurrentBuilds(),
  parameters([
    string(name: 'IMAGE_TAG', defaultValue: 'dev', description: 'Docker image tag used when publishing'),
    string(name: 'IMAGE_NAME', defaultValue: '', description: 'Docker image name used when publishing'),
    string(name: 'DOCKER_REGISTRY', defaultValue: 'quay.io/radiantsolutions', description: 'The place where docker images are published.'),
    text(name: 'ADHOC_PROJECT_YAML', defaultValue: '', description: 'Override the project vars used to generate documentation')
  ])
])

podTemplate(
  containers: [
    containerTemplate(
      name: 'git',
      image: 'alpine/git:latest',
      ttyEnabled: true,
      command: 'cat',
      envVars: [
        envVar(key: 'HOME', value: '/root')
      ]
    ),
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
        image: "${DOCKER_REGISTRY}/ktis-builder:2.0",
        name: 'builder',
        command: 'cat',
        ttyEnabled: true
    ),
    containerTemplate(
        name: 'site-builder',
        image: 'radiantsolutions/site-builder:latest',
        command: 'cat',
        ttyEnabled: true
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
    stage('Clone') {
      container('site-builder') {
        sh '''
          cd /mkdocs-site
          python3 tasks/clone-repos.py
        '''
      }
    }

    stage('Build site') {
      container('site-builder') {
      sh '''
        cd /mkdocs-site
        python3 tasks/generate.py
      '''
      }
    }

    stage('Build Service') {
      container('site-builder') {
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
