pipeline {
    agent any

    tools {
        maven 'Maven-3.8.8'
        jdk 'jdk-17'
    }

    environment {
        SONARQUBE = 'SonarQube'
        ARTIFACTORY_RELEASE = 'https://trial6dfohe.jfrog.io/artifactory/libs-release-local'
        ARTIFACTORY_SNAPSHOT = 'https://trial6dfohe.jfrog.io/artifactory/libs-snapshot-local'
        ARTIFACTORY_CREDENTIALS = 'artifactory-credentials'
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

        stage('Upload to Artifactory') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${ARTIFACTORY_CREDENTIALS}",
                                                  usernameVariable: 'USER',
                                                  passwordVariable: 'PASS')]) {
                    script {
                        // Determine if this is a snapshot build
                        def isSnapshot = readFile('pom.xml').contains('-SNAPSHOT')
                        def artifactRepo = isSnapshot ? "${ARTIFACTORY_SNAPSHOT}" : "${ARTIFACTORY_RELEASE}"
                        
                        echo "Is SNAPSHOT build: ${isSnapshot}"
                        echo "Using repository: ${artifactRepo}"
                        
                        // Use the correct WAR file name
                        def warFile = "target/myapp-1.0-SNAPSHOT.war"
                        def artifactName = "myapp-1.0-SNAPSHOT.war"
                        
                        // Try different Artifactory API endpoints
                        def uploadUrls = [
                            "${artifactRepo}/${artifactName}",
                            "${artifactRepo}/myapp/${artifactName}",
                            "${artifactRepo}/com/myapp/myapp/1.0-SNAPSHOT/${artifactName}"
                        ]
                        
                        def uploadSuccess = false
                        
                        for (uploadUrl in uploadUrls) {
                            try {
                                sh """
                                    echo "Trying to upload to: ${uploadUrl}"
                                    curl -f -u $USER:$PASS -X PUT -T ${warFile} "${uploadUrl}"
                                """
                                echo "✅ Upload successful to: ${uploadUrl}"
                                uploadSuccess = true
                                break
                            } catch (Exception e) {
                                echo "❌ Upload failed to: ${uploadUrl}"
                                echo "Error: ${e.getMessage()}"
                            }
                        }
                        
                        if (!uploadSuccess) {
                            // Fallback: simple upload without structured path
                            sh """
                                echo "Trying simple upload..."
                                curl -f -u $USER:$PASS -X PUT -T ${warFile} "${artifactRepo}/"
                            """
                        }
                    }
                }
            }
        }

        stage('Prepare for Deployment') {
            steps {
                script {
                    // Copy the WAR file to a known location for Ansible
                    sh """
                        cp target/myapp-1.0-SNAPSHOT.war myapp.war
                        ls -la myapp.war
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
                    extras: '--extra-vars "war_file_path=${WORKSPACE}/myapp.war"'
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
