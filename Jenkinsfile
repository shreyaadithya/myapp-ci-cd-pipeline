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
                        // Determine correct artifact name
                        def artifactFile = "myapp-1.0-SNAPSHOT.war"  // matches Maven build output
                        def artifactRepo = "${ARTIFACTORY_SNAPSHOT}" // using snapshot repo

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
                    inventory: 'hosts',             // your inventory file
                    playbook: 'deploy.yml',         // Ansible playbook
                    credentialsId: 'my-ssh-key',   // SSH private key in Jenkins
                    extraVars: [
                        artifact_name: "myapp-1.0-SNAPSHOT.war",
                        artifact_repo: "${ARTIFACTORY_SNAPSHOT}",
                        tomcat_path: "/opt/tomcat"
                    ]
                )
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline completed successfully!"
        }
        failure {
            echo "❌ Pipeline failed. Check the logs."
        }
    }
}
