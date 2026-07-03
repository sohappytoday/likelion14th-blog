pipeline {
  agent any

  options {
    disableConcurrentBuilds()   // 멱등성 보장
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '20'))
  }

  environment {
    IMAGE = 'ghcr.io/sohappytoday/likelion14th-blog'
    GITOPS_REPO = 'github.com/sohappytoday/cardinal-gitops.git'
  }

  stages {
    stage('이벤트 정보 확인') {
      steps {
        sh '''
          echo "BRANCH_NAME=${BRANCH_NAME}"
          echo "CHANGE_ID=${CHANGE_ID}"
          echo "CHANGE_BRANCH=${CHANGE_BRANCH}"
          echo "CHANGE_TARGET=${CHANGE_TARGET}"
        '''

        script {
          env.SHA = sh(
            script: 'git rev-parse --short HEAD',
            returnStdout: true
          ).trim()

          echo "빌드 SHA: ${env.SHA}"
        }
      }
    }

    /*
     * 1. dev 대상 PR
     * 2. staging 대상 PR
     * 3. staging 브랜치 직접 빌드
     *
     * 위 세 경우 모두 테스트 실행
     */
    stage('Test') {
      when {
        anyOf {
          changeRequest target: 'dev'
          changeRequest target: 'staging'
          branch 'staging'
        }
      }

      steps {
        sh 'chmod +x gradlew'
        sh './gradlew test --no-daemon'
      }
    }

    /*
     * dev 대상 PR에서는 애플리케이션 빌드까지 확인
     */
    stage('Application Build') {
      when {
        anyOf{
          changeRequest target: 'dev'
          changeRequest target: 'staging'
        }
      }

      steps {
        // Test Stage에서 테스트했으므로 중복 테스트 방지
        sh './gradlew build -x test --no-daemon'
      }
    }

    /*
     * PR이 아니라 실제 staging 브랜치가 변경된 경우에만
     * Docker 이미지 생성
     */
    stage('Docker Build') {
      when {
        branch 'staging'
      }

      steps {
        sh 'docker build -t "${IMAGE}:${SHA}" .'
      }
    }

    /*
     * 실제 staging push에서만 GHCR 업로드
     */
    stage('Push to GHCR') {
      when {
        branch 'staging'
      }

      steps {
        withCredentials([
          usernamePassword(
            credentialsId: 'github-pat',
            usernameVariable: 'GH_USER',
            passwordVariable: 'GH_TOKEN'
          )
        ]) {
          sh '''
            echo "$GH_TOKEN" |
              docker login ghcr.io \
                -u "$GH_USER" \
                --password-stdin

            docker push "${IMAGE}:${SHA}"
          '''
        }
      }
    }

    /*
     * 실제 staging push에서만 GitOps 저장소 변경
     */
    stage('Update Staging GitOps') {
      when {
        branch 'staging'
      }

      steps {
        withCredentials([
          usernamePassword(
            credentialsId: 'github-pat',
            usernameVariable: 'GH_USER',
            passwordVariable: 'GH_TOKEN'
          )
        ]) {
          sh '''
            rm -rf gitops-tmp

            git clone \
              "https://${GH_USER}:${GH_TOKEN}@${GITOPS_REPO}" \
              gitops-tmp

            cd gitops-tmp

            sed -i \
              "s|${IMAGE}:.*|${IMAGE}:${SHA}|" \
              overlays/staging/deployment.yaml

            git config user.email "jenkins@ci"
            git config user.name "jenkins-ci"

            git add overlays/staging/deployment.yaml

            if git diff --cached --quiet; then
              echo "GitOps 변경 사항이 없습니다."
            else
              git commit -m "deploy(staging): ${SHA}"
              git push origin main
            fi
          '''
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