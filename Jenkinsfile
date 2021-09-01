pipeline {

    agent {
        kubernetes {
            yamlFile 'kubernetesPod.yaml'
        }
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '3'))
    }

    stages {

        stage('Build') {

            steps {

                container('builder') {

                    configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {

                        sh 'mvn -s $MAVEN_SETTINGS_RSB -U clean package -DskipTests'

                    }
                }
            }
        }

        stage('Test') {

            steps {

                container('builder') {

                    configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {

                        sh 'mvn -s $MAVEN_SETTINGS_RSB test'

                    }
                }
            }
        }
    }
    
    post {
        success {
            jacoco(execPattern: 'target/jacoco.exec')
        }
    }

}
