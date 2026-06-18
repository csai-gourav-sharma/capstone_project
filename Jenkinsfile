```groovy
pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Workspace Check') {
            steps {
                bat 'cd'
                bat 'dir'
            }
        }

        stage('Build & Compile') {
            steps {
                echo 'Compiling project...'
                bat 'mvn clean compile -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests...'
                bat 'mvn test'
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker images...'
                bat 'docker compose -f ci-cd\\docker-compose.yml build'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying application...'
                bat 'docker compose -f ci-cd\\docker-compose.yml down'
                bat 'docker compose -f ci-cd\\docker-compose.yml up -d'
            }
        }
    }

    post {
        success {
            echo 'Build, Test, Docker Build and Deployment completed successfully!'
        }

        failure {
            echo 'Pipeline failed. Check the console output for details.'
        }

        always {
            cleanWs()
        }
    }
}
```
