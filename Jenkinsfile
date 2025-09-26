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
                        
                        // Determine if this is a snapshot build
                        def isSnapshot = readFile('pom.xml').contains('-SNAPSHOT')
                        def artifactRepo = isSnapshot ? "${ARTIFACTORY_SNAPSHOT}" : "${ARTIFACTORY_RELEASE}"
                        
                        echo "Is SNAPSHOT build: ${isSnapshot}"
                        echo "Using repository: ${artifactRepo}"
                        
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
                            error "No WAR file found in target directory!"
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
