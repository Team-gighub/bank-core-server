pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "rudska6/bank-core"
        DOCKER_TAG = "latest"
        EC2_HOST = "ssh.gighub-bank.site"       // 백엔드 서버 IP
        EC2_KEY = "deploy-key"                  // Jenkins SSH key ID
        COMPOSE_FILE = "docker-compose.yml"
        ENV_FILE = ".env"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'feature/deploy-setting',
                    credentialsId: 'github',
                    url: 'https://github.com/Team-gighub/bank-core-server'
            }
        }

        stage('Build JAR') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean build'
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
            }
        }

        stage('Docker Login') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-login',
                                usernameVariable: 'DOCKER_USER',
                                passwordVariable: 'DOCKER_PASS')]) {
                    sh "echo \"$DOCKER_PASS\" | docker login -u \"$DOCKER_USER\" --password-stdin"
                }
            }
        }

        stage('Docker Push') {
            steps {
                sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
            }
        }

        stage('Deploy to EC2 (docker-compose)') {
            steps {
                sshagent(credentials: ['onprem-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ssh.gighub-bank.site '
                        cd ~/bank-core || mkdir ~/bank-core && cd ~/bank-core;

                        # 최신 이미지 pull
                        docker pull bank-core;

                        docker rm -f bank-core || true

                        docker compose up -d bank-core
                    '
                    """
                }
            }
        }
    }
}
