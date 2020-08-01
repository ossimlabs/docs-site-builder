properties([
  buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '3', daysToKeepStr: '', numToKeepStr: '20')),
  disableConcurrentBuilds(),
  parameters([
    string(name: 'VERSION', defaultValue: 'test', description: 'Docker image tag used when publishing'),
    string(name: 'DOCKER_REGISTRY_PUSH', defaultValue: 'nexus-docker-private-hosted.ossim.io', description: 'Docker registry push url.')
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
    )
  ],
  volumes: [
    hostPathVolume(
      hostPath: '/var/run/docker.sock',
      mountPath: '/var/run/docker.sock'
    )
  ]
) {
  node(POD_LABEL) {
    stage("Checkout branch $BRANCH_NAME")
    {
      checkout(scm)
    }

    stage('Copy Files') {
      container('docker') {
        sh """
          cp -r src/ docker/builder-image/
          cp -r docker/docs-service/ docker/builder-image/
          cp requirements.txt docker/builder-image/
        """
      }
    }

    stage('Docker Build') {
      container('docker') {
        sh """
          cd docker/builder-image
          docker build . --network=host -t ${DOCKER_REGISTRY_PUSH}/docs-site-builder:${VERSION}
        """
      }
    }

    stage('Docker Publish') {
      container('docker') {
        withDockerRegistry(credentialsId: 'dockerCredentials', url: "https://${DOCKER_REGISTRY_PUSH}") {
          sh """
            docker push ${DOCKER_REGISTRY_PUSH}/docs-site-builder:${VERSION}
          """
        }
      }
    }

    stage("Clean Workspace") {
      step([$class: 'WsCleanup'])
    }
  }
}