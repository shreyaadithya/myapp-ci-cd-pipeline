pipeline {
    agent any

    environment {
        MVN_HOME = tool name: 'Maven', type: 'maven'
        ARTIFACTORY_USER = credentials('artifactory_user')   // Jenkins credential ID
        ARTIFACTORY_API_KEY = credentials('artifactory_api_key')
        ARTIFACT_NAME = "myapp-1.0-SNAPSHOT.war"
        ARTIFACTORY_REPO = "libs-snapshot-local"
        ARTIFACTORY_URL = "https://trial6dfohe.jfrog.io/artifactory/${ARTIFACTORY_REPO}/${ARTIFACT_NAME}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/shreyaadithya/myapp-ci-cd-pipeline.git'
            }
        }

        stage('Build') {
            steps {
                sh "${MVN_HOME}/bin/mvn clean package -DskipTests"
            }
        }

        stage('SonarQube Analysis') {
            environment {
                SONAR_HOST_URL = 'http://54.91.93.239:9000'
                SONAR_AUTH_TOKEN = credentials('sonarqube_token')
            }
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh "${MVN_HOME}/bin/mvn sonar:sonar -Dsonar.projectKey=myapp"
                }
            }
        }

        stage('Upload to Artifactory') {
            steps {
                sh """
                    curl -u ${ARTIFACTORY_USER}:${ARTIFACTORY_API_KEY} \
                         -T target/${ARTIFACT_NAME} \
                         ${ARTIFACTORY_URL}
                """
            }
        }

        stage('Deploy using Ansible') {
            steps {
                ansiblePlaybook(
                    playbook: 'deploy.yml',
                    inventory: 'hosts',
                    extras: "-e artifactory_user=${ARTIFACTORY_USER} -e artifactory_api_key=${ARTIFACTORY_API_KEY} -e artifact_name=${ARTIFACT_NAME}"
                )
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline succeeded"
        }
        failure {
            echo "❌ Pipeline failed. Check logs."
        }
    }
}
