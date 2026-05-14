pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    environment {
        SONAR_PROJECT_KEY = 'vulncheck'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                dir('vulncheck') {
                    sh 'mvn clean compile -DskipTests'
                }
            }
        }

        stage('Test') {
            steps {
                dir('vulncheck') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'vulncheck/**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('vulncheck') {
                    withSonarQubeEnv('SonarQube') {
                        sh 'mvn sonar:sonar'
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline exitoso - Middleware VulnChecker listo'
        }

        failure {
            echo 'Pipeline fallido - revisar logs y Quality Gate'
        }
    }
}
