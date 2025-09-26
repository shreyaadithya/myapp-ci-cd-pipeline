pipeline {
    agent any

    tools {
        maven 'Maven-3.8.8'
        jdk 'jdk-17'
    }

    environment {
        SONARQUBE = 'SonarQube'
        ARTIFACTORY_URL = 'https://trial6dfohe.jfrog.io/artifactory' // Replace with your Artifactory URL
        ARTIFACTORY_CREDENTIALS = 'artifactory-credentials'       // Jenkins credential ID
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/shreyaadithya/myapp-ci-cd-pipeline.git'
            }
        }

        stage('Build with Maven') {
            steps {
                sh "mvn clean package"
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE}") {
                    sh "mvn sonar:sonar -Dsonar.projectKey=myapp"
                }
            }
        }

        stage('Upload to Artifactory') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${ARTIFACTORY_CREDENTIALS}", 
                                                  usernameVariable: 'USER', 
                                                  passwordVariable: 'PASS')]) {
                    sh """
                        curl -u $USER:$PASS -T target/myapp-1.0-SNAPSHOT.war \
                        "${ARTIFACTORY_URL}/libs-snapshot-local/myapp-1.0-SNAPSHOT.war"
                    """
                }
            }
        }

        stage('Deploy using Ansible') {
            steps {
                ansiblePlaybook(
                    credentialsId: 'ssh-key',  // Make sure this exists in Jenkins
                    inventory: 'hosts',
                    playbook: 'deploy.yml'
                )
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully!"
        }
        failure {
            echo "Pipeline failed. Please check the logs."
        }
    }
}
