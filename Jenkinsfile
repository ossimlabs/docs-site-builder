properties([
    parameters ([
        string( name: 'BUILD_NODE', defaultValue: 'omar-build', description: 'The build node to run on' ),
        booleanParam( name: 'CLEAN_WORKSPACE', defaultValue: true, description: 'Clean the workspace at the end of the run' )
    ]),
    pipelineTriggers([
            [ $class: "GitHubPushTrigger" ]
    ]),
    [ $class: 'GithubProjectProperty', displayName: '', projectUrlStr: 'https://github.com/ossimlabs/omar-docs' ],
    buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '3', daysToKeepStr: '', numToKeepStr: '20')),
    disableConcurrentBuilds()
])

node( "${ BUILD_NODE }" ) {
    stage( "Checkout branch $BRANCH_NAME" ) {
        checkout( scm )
    }

    stage( "Load Variables" ) {
        withCredentials([ string( credentialsId: 'o2-artifact-project', variable: 'o2ArtifactProject' ) ]) {
            step ([
                $class: "CopyArtifact",
                projectName: o2ArtifactProject,
                filter: "common-variables.groovy",
                flatten: true
            ])
        }

        load "common-variables.groovy"
    }

    stage ( "Assemble" ) {
        withCredentials([[
            $class: 'UsernamePasswordMultiBinding',
            credentialsId: 'openshiftCredentials',
            usernameVariable: 'OPENSHIFT_USERNAME',
            passwordVariable: 'OPENSHIFT_PASSWORD'
        ]]) {
            sh """
                oc login $OPENSHIFT_URL -u $OPENSHIFT_USERNAME -p $OPENSHIFT_PASSWORD
                python3 createFiles.py
                tar cfz docs.tgz site
            """
        }
        archiveArtifacts "docs.tgz"
    }

    stage ("Publish Docker App") {
        withCredentials([[
            $class: 'UsernamePasswordMultiBinding',
            credentialsId: 'dockerCredentials',
            usernameVariable: 'DOCKER_REGISTRY_USERNAME',
            passwordVariable: 'DOCKER_REGISTRY_PASSWORD'
        ]]) {
            sh """
                mv docs.tgz ./docker
                gradle pushDockerImage -PossimMavenProxy=${OSSIM_MAVEN_PROXY}
            """
        }
    }

    try {
        stage ( "OpenShift Tag Image" ) {
            withCredentials([[
                $class: 'UsernamePasswordMultiBinding',
                credentialsId: 'openshiftCredentials',
                usernameVariable: 'OPENSHIFT_USERNAME',
                passwordVariable: 'OPENSHIFT_PASSWORD'
            ]]) {
                sh """
                    gradle openshiftTagImage -PossimMavenProxy=${OSSIM_MAVEN_PROXY}
                """
            }
        }
    } catch ( e ) {
        echo e.toString()
    }

    stage( "Clean Workspace" ) {
        if ( "${ CLEAN_WORKSPACE }" == "true" )
            step([ $class: 'WsCleanup' ])
    }
}


properties([
  buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '3', daysToKeepStr: '', numToKeepStr: '20')),
  disableConcurrentBuilds(),
  parameters([
      string(name: 'IMAGE_TAG', defaultValue: 'dev', description: 'Docker image tag used when publishing'),
      text(name: 'ADHOC_PROJECT_YAML', defaultValue: '', description: 'Override the project vars used to generate documentation')
  ])
])



properties([
  buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '3', daysToKeepStr: '', numToKeepStr: '20')),
  disableConcurrentBuilds(),
  parameters([
      string(defaultValue: 'nexus-docker-private-hosted.ktis.radiantblue.local', description: 'The docker repo to use', name: 'DOCKER_REGISTRY', trim: false),
      string(defaultValue: 'ktis-javadoc', description: 'The branch to use', name: 'GIT_BRANCH', trim: false),
      string(defaultValue: 'javadoc.tgz', description: 'artifact ', name: 'WEB_ARTIFACT', trim: false)
  ]),
  pipelineTriggers([
    pollSCM('* * * * *')
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
      container('git') {
        sh '''
          mkdir ~/.ssh
          cp -H /bitbucket_secret/ssh-privatekey ~/.ssh/id_rsa
          echo -e "Host *\n    StrictHostKeyChecking=no" > ~/.ssh/config
          git clone git@bitbucket.org:radiantsolutions/build.git --single-branch --branch ${GIT_BRANCH}
          cd build
          cat clone.sh
          sh ./clone.sh --branch ${GIT_BRANCH}
        '''
      }
    }

    stage('Build') {
      container('builder') {
        sh '''
          cd build
          cd microservices
          git branch -vvv
          cd ..
          ./gradlew --no-daemon javadoc

          for i in $(find . -wholename "*build/docs/javadoc" -type d)
          do
              mkdir -p ../www/${i} && cp -r ${i} ../www/${i}/..
          done
          cd ..
          tar czf ${WEB_ARTIFACT} www/
          '''
        archiveArtifacts WEB_ARTIFACT
      }
    }
  }
}
