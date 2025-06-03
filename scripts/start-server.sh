#!/bin/bash

echo "----- 서버 배포 시작 -----"
cd /home/ubuntu/health
docker compose down || true
docker pull {ECR Repository 주소}/springboot:latest
docker pull {ECR Repository 주소}/vue:latest
docker pull {ECR Repository 주소}/ES:latest
docker compose up -d
echo "----- 서버 배포 끝 -----"