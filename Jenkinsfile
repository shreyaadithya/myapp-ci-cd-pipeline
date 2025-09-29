pipeline {
    agent any

    tools {
        maven 'Maven-3.8.8'
        jdk 'jdk-17'
    }

    environment {
        SONARQUBE = 'SonarQube'
        ANSIBLE_HOST_KEY_CHECKING = 'False'
        APP_NAME = 'myapp'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'working', url: 'https://github.com/shreyaadithya/myapp-ci-cd-pipeline.git'
            }
        }

        stage('Build with Maven') {
            steps {
                sh "mvn clean package -DskipTests"
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE}") {
                    sh "mvn sonar:sonar -Dsonar.projectKey=myapp"
                }
            }
        }

        stage('Prepare for Deployment') {
            steps {
                script {
                    // Prepare the webapp directory with WAR file
                    sh """
                        mkdir -p webapp
                        cp target/myapp-1.0-SNAPSHOT.war webapp/
                        echo "Application WAR file prepared for deployment:"
                        ls -la webapp/
                    """
                }
            }
        }

        stage('Deploy using Ansible') {
            steps {
                ansiblePlaybook(
                    playbook: 'deploy.yml',
                    inventory: 'hosts',
                    credentialsId: 'my-ssh-key',
                    extras: "--extra-vars 'app_name=${APP_NAME}'"
                )
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline completed successfully!"
        }
        failure {
            echo "❌ Pipeline failed. Please check the logs."
        }
    }
}
