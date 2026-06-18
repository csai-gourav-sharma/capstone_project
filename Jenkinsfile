pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
        jdk 'JDK 17'
    }

    stages {
        stage('Checkout') {
            steps {
                // In actual deployment, this pulls the code from SCM.
                // For local Jenkins pipeline runs, we can skip or run checkout scm.
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building backend microservices...'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests...'
                sh 'mvn test'
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker images...'
                sh 'docker compose -f ci-cd/docker-compose.yml build'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying services with Docker Compose...'
                sh 'docker compose -f ci-cd/docker-compose.yml up -d'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}
