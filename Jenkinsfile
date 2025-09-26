pipeline {
    agent any

    tools {
        maven 'Maven-3.8.8'
        jdk 'jdk-17'
    }

    environment {
        SONARQUBE = 'SonarQube'
        ARTIFACTORY_RELEASE = 'https://trial6dfohe.jfrog.io/artifactory/libs-release-local-libs-release'
        ARTIFACTORY_SNAPSHOT = 'https://trial6dfohe.jfrog.io/artifactory/libs-release-local-libs-snapshot'
        ARTIFACTORY_CREDENTIALS = 'artifactory-credentials'  // Jenkins credential ID
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
                    script {
                        // Determine if the build is a SNAPSHOT or RELEASE
                        def isSnapshot = env.BUILD_TAG?.contains('SNAPSHOT') || true // You can replace with actual logic
                        def artifactRepo = isSnapshot ? "${ARTIFACTORY_SNAPSHOT}" : "${ARTIFACTORY_RELEASE}"
                        def artifactFile = isSnapshot ? "myapp-1.0-SNAPSHOT.war" : "myapp-1.0.war"

                        sh """
                            echo "Uploading ${artifactFile} to ${artifactRepo}"
                            curl -u $USER:$PASS -T target/${artifactFile} "${artifactRepo}/${artifactFile}"
                        """
                    }
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
