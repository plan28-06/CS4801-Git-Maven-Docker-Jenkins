pipeline {

    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Maven Build and Test') {
            steps {
                bat 'mvn clean package'
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker build -t student-management:1.0 .'
            }
        }

        stage('Docker Deploy') {
            steps {
                bat '''
                    docker stop student-app || true
                    docker rm student-app || true
                    docker run -d -p 8080:8080 --name student-app student-management:1.0
                '''
            }
        }
    }
}