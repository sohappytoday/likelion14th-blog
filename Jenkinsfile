pipeline {
  agent any

  environment {
    IMAGE = "ghcr.io/sohappytoday/likelion14th-blog"
    GITOPS_REPO = "github.com/sohappytoday/cardinal-gitops.git"
  }

  stages {
    // commit sha 확인
    stage('SHA 확인') {
      steps {
        script {
          env.SHA = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
          echo "빌드 태그: ${env.SHA}"
        }
      }
    }

    // 빌드 및 테스트
    stage('Test') {
      steps {
        sh 'chmod +x gradlew && ./gradlew test --no-daemon'
      }
    }

    // docker 이미지 생성
    stage('Build') {
      steps {
        sh "docker build -t ${IMAGE}:${SHA} ."
      }
    }

    // ghcr에 push
    stage('Push to GHCR') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'github-pat',
          usernameVariable: 'GH_USER', passwordVariable: 'GH_TOKEN')]) {
          sh 'echo "$GH_TOKEN" | docker login ghcr.io -u "$GH_USER" --password-stdin'
          sh "docker push ${IMAGE}:${SHA}"
        }
      }
    }

    stage('Update GitOps') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'github-pat',
          usernameVariable: 'GH_USER', passwordVariable: 'GH_TOKEN')]) {
          sh """
            rm -rf gitops-tmp
            git clone https://\${GH_USER}:\${GH_TOKEN}@${GITOPS_REPO} gitops-tmp
            cd gitops-tmp
            sed -i 's|${IMAGE}:.*|${IMAGE}:${SHA}|' overlays/staging/deployment.yaml
            git config user.email "jenkins@ci"
            git config user.name "jenkins-ci"
            git add -A
            git commit -m "deploy: ${SHA}" || echo "no change"
            git push origin main
          """
        }
      }
    }
  }

  post {
    always {
      sh 'docker logout ghcr.io || true'
      cleanWs()
    }
  }
}