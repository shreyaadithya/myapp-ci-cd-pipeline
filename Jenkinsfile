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
                        // Extract actual version from pom.xml
                        def version = sh(
                            script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
                            returnStdout: true
                        ).trim()

                        def isSnapshot = version.endsWith("SNAPSHOT")
                        def artifactRepo = isSnapshot ? "${ARTIFACTORY_SNAPSHOT}" : "${ARTIFACTORY_RELEASE}"
                        def artifactFile = "myapp-${version}.war"

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
                    inventory: 'hosts',
                    playbook: 'deploy.yml',
                    credentialsId: 'my-ssh-key'
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
