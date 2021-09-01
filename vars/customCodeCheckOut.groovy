def call() {
    checkout([$class: 'GitSCM', branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/suryan14k/jenkins-ci-cd-shared-libraries.git']]])
}
