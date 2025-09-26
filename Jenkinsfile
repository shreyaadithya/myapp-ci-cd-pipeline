pipeline {
    agent any

    tools {
        maven 'Maven-3.8.8'
        jdk 'jdk-17'
    }

    environment {
        SONARQUBE = 'SonarQube'
        ARTIFACTORY = 'artifactory-server' // Must match Artifactory server ID in Jenkins
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

        //stage('Upload to Artifactory') {
            //steps {
                //script {
                    //def server = Artifactory.server("${ARTIFACTORY}")

                    //def rtMaven = Artifactory.newMavenBuild()
                    //rtMaven.tool = 'Maven-3.8.8'

                    //// Configure resolver and deployer
                    //rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server
                    //rtMaven.resolver releaseRepo: 'libs-release', snapshotRepo: 'libs-snapshot', server: server

                    //// Run Maven build & publish to Artifactory
                    //rtMaven.run pom: 'pom.xml', goals: 'clean install'
                    //server.publishBuildInfo(rtMaven)
               // }
           // }
       // }
        stage('Upload to Artifactory') {
    steps {
        withCredentials([usernamePassword(credentialsId: 'artifactory-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
            sh """
                curl -u $USER:$PASS -T target/myapp-1.0-SNAPSHOT.war \
                "http://<your-artifactory-server>:8081/artifactory/libs-snapshot-local/myapp-1.0-SNAPSHOT.war"
            """
        }
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
