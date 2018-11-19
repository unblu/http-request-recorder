#!/usr/bin/env groovy

pipeline {
    agent { node { label 'master' } }

    stages {
        stage('Build') {
            steps {
                sh './gradlew clean build'
                junit '**/build/test-results/**/*.xml'
                archiveArtifacts artifacts: 'build/libs/*.jar'
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