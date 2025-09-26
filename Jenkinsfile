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
        ARTIFACTORY_CREDENTIALS = 'artifactory-credentials'  // Jenkins credential ID
        ANSIBLE_HOST_KEY_CHECKING = 'False' // Disable host key checking in CI/CD
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

        stage('Diagnose Build Output') {
            steps {
                sh """
                    echo "=== Checking build output ==="
                    ls -la target/
                    find target/ -name '*.war' -type f
                """
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
                        // List files for debugging
                        sh "ls -la target/"
                        
                        def isSnapshot = env.BRANCH_NAME?.contains("SNAPSHOT") || env.BRANCH_NAME == "main"
                        def artifactRepo = isSnapshot ? "${ARTIFACTORY_SNAPSHOT}" : "${ARTIFACTORY_RELEASE}"
                        
                        // Dynamically find the WAR file
                        def warFiles = sh(script: "find target/ -name '*.war' -type f", returnStdout: true).trim()
                        
                        if (warFiles) {
                            def warFile = warFiles.split('\n')[0].trim()
                            def artifactFile = warFile.replace('target/', '')
                            
                            sh """
                                echo "Found WAR file: ${warFile}"
                                echo "Uploading ${artifactFile} to ${artifactRepo}"
                                curl -u $USER:$PASS -T ${warFile} "${artifactRepo}/${artifactFile}"
                            """
                        } else {
                            // Fallback: try common WAR file names
                            def commonWarNames = ["myapp.war", "myapp-1.0.war", "myapp-1.0-SNAPSHOT.war"]
                            def foundWar = false
                            
                            for (warName in commonWarNames) {
                                def warPath = "target/${warName}"
                                if (fileExists(warPath)) {
                                    sh """
                                        echo "Uploading ${warName} to ${artifactRepo}"
                                        curl -u $USER:$PASS -T ${warPath} "${artifactRepo}/${warName}"
                                    """
                                    foundWar = true
                                    break
                                }
                            }
                            
                            if (!foundWar) {
                                error "No WAR file found in target directory! Available files: ${sh(script: 'ls -la target/', returnStdout: true)}"
                            }
                        }
                    }
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
