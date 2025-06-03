# 🛡️ 게시판 프로젝트

보안과 성능 최적화에 집중한 실험적 게시판 웹 애플리케이션입니다.  
JWT 인증, Redis 기반 캐싱, Elasticsearch 검색, Docker 기반 배포 등  
실제 서비스에서 마주치는 문제를 시뮬레이션하고 해결 방안을 적용한 프로젝트입니다.

---

## 📌 프로젝트 개요

| 항목 | 내용 |
|------|------|
| **프로젝트명** | Health 게시판 |
| **설명** | 보안과 성능을 실험하기 위한 기술 통합형 게시판 |
| **작업 기간** | 2025.03 ~ (현재 진행 중) |
| **인원 구성** | 개인 프로젝트 (1명) |

---

## 🎯 개발 목적

단순한 게시판 구현에 그치지 않고,  
실제 서비스에서 발생하는 **보안**, **동시성**, **검색 성능**, **배포 자동화** 등의 문제를 다루며  
**웹 서비스 전반의 라이프사이클을 설계/운영**하는 것이 목표입니다.

---

## 🛠️ 사용 기술 스택

### 📌 Backend
- **Spring Boot**: `v3.4.3`
- **Spring Security**: `v6.2.2`
- **Spring Data JPA**
- **Spring Data Redis** (DockerHub 기준 `latest`)
- **Spring Data Elasticsearch**: `v8.17.2`
- **Spring Batch**:  `v3.4.3`
- **JWT (JJWT)**: `v0.12.3`
- **OAuth2.0**: 포함된 Spring Security 모듈
- **MySQL**: (DockerHub 기준 `latest`)
- **Redis / Elasticsearch / MySQL**: Docker 기반 컨테이너 운영

### 💻 Frontend
- **Vue.js**: `v3.2.13`

### 📦 DevOps
- **Docker**
- **ECR** → **EC2 배포**

---

## 🔐 사용자 인증 및 보안 기능

- Spring Security + JWT를 통한 XSS, CSRF 보안 처리 적용
- OAuth2.0 기반의 **소셜 로그인**
- 로그인 시 **비밀번호 불일치 문제 해결을 위한 Factory Method** 설계
- **Refresh Token Rotation** 및 **블랙리스트 관리**
- 디바이스 별 JWT 발급 로직 구성

---

## ⚙️ 공통 로직 및 최적화

- `Pagination` 시 발생하는 N+1 문제 해결  
- `Batch Size`, `Fetch Join`, `jpql` 활용
- 조회수 & 추천수 기능의 **동시성 트래픽 문제 해결**
- 무분별한 새로고침에 따른 **조회수 증가 제한**
- **Elasticsearch 기반 유연한 고속 검색 기능** 구현
- Batch를 통한 스케줄링 구현

---

## 📦 아키텍처
![architecture](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FdhYj2I%2FbtsOl0ckxuq%2Fcqx73GkiC6bXTScKvthqPK%2Fimg.png)

## 🗂️ ERD
![ERD](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FH0Awu%2FbtsOlPhMSwy%2FzNi24rYVhZB4tNkaBOvMsk%2Fimg.png)

## 🎨 UI
| 회원가입 | 로그인 | 메인화면 |
|----------|--------|-----------|
| ![회원가입](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fcb2G1s%2FbtsNip6RW0I%2Fkc39YE0Yv7e5UyDrgEJYbk%2Fimg.png) | ![로그인](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FF9Wmb%2FbtsNG1jwtUB%2FAkYIXfHKSSA19negRJYnX0%2Fimg.png) | ![메인화면](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbIPUvc%2FbtsNg9h6HaK%2F363cQOtZRUt4ousif5ynN0%2Fimg.png) |

| 게시글 작성 | 게시글 보기 | 댓글 |
|--------------|--------------|--------|
| ![글작성](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FoymCN%2FbtsNiATveEy%2FmjouMkFHdgGvF7KPFLEBMk%2Fimg.png) | ![게시글보기](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FR4k8c%2FbtsNjAMrbYE%2FhTuS7rmxymgtAqqcCzktk0%2Fimg.png) | ![댓글](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FEgh3P%2FbtsNisPJr1B%2FM2X7kXBLlZCmXYjlgka5BK%2Fimg.png) |