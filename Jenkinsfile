pipeline {
    agent any

    tools {
        maven 'Maven-3.8.8'
        jdk 'jdk-17'
    }

    environment {
        SONARQUBE = 'SonarQube'
        ARTIFACTORY = 'Artifactory'
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
                withSonarQubeEnv('SonarQube') {
                    sh "mvn sonar:sonar -Dsonar.projectKey=myapp"
                }
            }
        }

        //stage('Quality Gate') {
            //steps {
                //timeout(time: 5, unit: 'MINUTES') {
                    //waitForQualityGate abortPipeline: true
                //}
            //}
        //}

        stage('Upload to Artifactory') {
            steps {
                rtMavenRun (
                    tool: 'Maven-3.8.8',
                    pom: 'pom.xml',
                    goals: 'clean install',
                    resolverId: 'maven-resolver',
                    deployerId: 'maven-deployer'
                )
            }
        }

        stage('Deploy using Ansible') {
            steps {
                ansiblePlaybook(
                    credentialsId: 'ssh-key',
                    inventory: 'hosts',
                    playbook: 'deploy.yml'
                )
            }
        }
    }
}
