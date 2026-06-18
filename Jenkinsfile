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
                bat 'dir stationery-management-system'
            }
        }

        stage('Build & Compile') {
            steps {
                echo 'Compiling project...'
                dir('stationery-management-system') {
                    bat 'mvn clean compile -DskipTests'
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests...'
                dir('stationery-management-system') {
                    bat 'mvn test'
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker images...'
                dir('stationery-management-system/ci-cd') {
                    bat 'docker compose build'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying application...'
                dir('stationery-management-system/ci-cd') {
                    bat 'docker compose down'
                    bat 'docker compose up -d'
                }
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