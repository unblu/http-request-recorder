#!/usr/bin/env groovy

pipeline {
    agent { node { label 'master' } }

    stages {
        stage('Build') {
            steps {
                sh './gradlew clean build'
                junit '**/build/test-results/**/*.xml'
                archiveArtifacts artifacts: 'cli/build/libs/hrr.jar'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '5'))
    }
}