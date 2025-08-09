# 🛡️ 보안·성능 최적화 게시판 프로젝트

JWT 인증, Redis 캐싱, Elasticsearch 검색, Docker 기반 자동 배포 등  
**실제 서비스 환경에서 발생하는 문제를 시뮬레이션하고 해결 방안을 적용**한 실험형 게시판 웹 애플리케이션입니다.

---

## 📌 프로젝트 개요

| 항목 | 내용 |
|------|------|
| **프로젝트명** | 보안·성능 최적화 게시판 |
| **목표** | 보안·동시성·검색 성능·배포 자동화 등 서비스 전반 문제 해결 |
| **작업 기간** | 2025.03 ~ 2025.06 |
| **인원 구성** | 개인 프로젝트 (1명) |
| **저장소** | [GitHub](https://github.com/leesanghak123/Health) |

---

## 🎯 개발 목적

단순 게시판 구현을 넘어,  
- **보안 위협 대응** (JWT, XSS/CSRF 방어, 소셜 로그인)  
- **동시성 제어** (조회/추천 카운트 정합성 유지)  
- **검색 성능 향상** (Elasticsearch)  
- **배포 효율성** (CI/CD 자동화)  

등 실제 서비스 라이프사이클 전반을 다루며 설계와 운영 경험을 쌓는 것이 목적입니다.

---

## 🛠️ 기술 스택

### 📌 Backend
- **Spring Boot** `3.4.3`
- **Spring Security** `6.2.2` (JWT, OAuth 2.0)
- **Spring Data JPA**
- **Spring Data Redis**
- **Spring Data Elasticsearch** `8.17.2`
- **Spring Batch**
- **MySQL**

### 💻 Frontend
- **Vue.js** `3.2.13`

### 🗄️ Database & Caching
- **MySQL** (Docker)
- **Redis** (Docker)
- **Elasticsearch** (Docker)

### ⚙️ DevOps
- **Docker & Docker Compose**
- **GitHub Actions**
- **AWS EC2, ECR, S3, CodeDeploy**

---

## 🔐 주요 기능

### 1. 인증·인가 보안 강화
- JWT + Refresh Token Rotation
- Redis 기반 토큰 화이트리스트 관리
- OAuth 2.0 소셜 로그인
- HTML Escaping / Sanitization, CSP 적용
- 디바이스 UUID 기반 토큰 발급

### 2. 동시성 문제 해결
- Redis 캐싱 기반 조회/추천 수 집계
- Spring Batch로 일정 주기 DB 반영
- 무분별한 새로고침에 따른 조회수 폭증 방지

### 3. 검색 기능 고도화
- Elasticsearch 기반 Full Text Search
- 동의어·다국어 처리 지원
- MySQL LIKE 대비 평균 응답 속도 6.3% 단축

### 4. 배포 자동화
- GitHub Actions + Docker 빌드/배포 파이프라인
- Push → EC2 자동 배포
- 배포 과정의 일관성과 안정성 확보

---

## ⚡ 성능 개선 결과

| 항목 | 개선 전 | 개선 후 | 개선율 |
|------|--------|--------|--------|
| 대량 트래픽 처리 성능 | 초당 50건 | 초당 500건 | **900% ↑** |
| 검색 응답 속도 | MySQL LIKE 쿼리 기준 | Elasticsearch 적용 | **6.3% 단축** |
| 조회/추천 데이터 정합성 | 동시성 문제로 오류 발생 | Redis + Batch 스케줄링 | **100% 안정화** |

---

## 📦 아키텍처
![architecture](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FdhYj2I%2FbtsOl0ckxuq%2Fcqx73GkiC6bXTScKvthqPK%2Fimg.png)

---

## 🗂️ ERD
![ERD](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FH0Awu%2FbtsOlPhMSwy%2FzNi24rYVhZB4tNkaBOvMsk%2Fimg.png)

---

## 🎨 주요 화면

| 회원가입 | 로그인 | 메인화면 |
|----------|--------|-----------|
| ![회원가입](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fcb2G1s%2FbtsNip6RW0I%2Fkc39YE0Yv7e5UyDrgEJYbk%2Fimg.png) | ![로그인](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FF9Wmb%2FbtsNG1jwtUB%2FAkYIXfHKSSA19negRJYnX0%2Fimg.png) | ![메인화면](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbIPUvc%2FbtsNg9h6HaK%2F363cQOtZRUt4ousif5ynN0%2Fimg.png) |

| 게시글 작성 | 게시글 보기 | 댓글 |
|--------------|--------------|--------|
| ![글작성](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FoymCN%2FbtsNiATveEy%2FmjouMkFHdgGvF7KPFLEBMk%2Fimg.png) | ![게시글보기](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FR4k8c%2FbtsNjAMrbYE%2FhTuS7rmxymgtAqqcCzktk0%2Fimg.png) | ![댓글](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FEgh3P%2FbtsNisPJr1B%2FM2X7kXBLlZCmXYjlgka5BK%2Fimg.png) |

---

## 📝 배운 점
- **성능 최적화와 데이터 정합성의 균형**이 중요함을 체감  
- 실제 서비스 환경을 고려한 구조 설계 경험 축적  
- DevOps 자동화 파이프라인 설계 경험 확보