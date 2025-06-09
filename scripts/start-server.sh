#!/bin/bash

echo "----- 서버 배포 시작 -----"
cd /home/ubuntu/health
docker compose down || true
docker pull 992382367281.dkr.ecr.ap-northeast-2.amazonaws.com/springboot:latest
docker pull 992382367281.dkr.ecr.ap-northeast-2.amazonaws.com/vue:latest
docker pull 992382367281.dkr.ecr.ap-northeast-2.amazonaws.com/es:latest
docker compose up -d
echo "----- 서버 배포 끝 -----"