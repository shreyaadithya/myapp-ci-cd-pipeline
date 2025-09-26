pipeline {
    agent any

    tools {
        maven 'Maven-3.8.8'
        jdk 'jdk-17'
    }

    environment {
        SONARQUBE = 'SonarQube'
        ANSIBLE_HOST_KEY_CHECKING = 'False'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/shreyaadithya/myapp-ci-cd-pipeline.git'
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
                    // Create a simple HTML version for HTTPD deployment
                    sh """
                        mkdir -p webapp
                        cp target/myapp-1.0-SNAPSHOT.war webapp/ || true
                        # Create a simple index.html for testing
                        echo '<html><body><h1>MyApp Deployment Successful!</h1><p>Application is running on Apache HTTPD</p><p>WAR file: myapp-1.0-SNAPSHOT.war</p></body></html>' > webapp/index.html
                        echo "Web application prepared for deployment:"
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
                    credentialsId: 'my-ssh-key'
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
