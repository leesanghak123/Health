# Health

# 🏋️‍♀️ Health App

**Health에 필요한 정보들을 담은 Web Application입니다.**

---

## 🧠 만들게 된 이유

> 많은 정보들이 쏟아지면서 Health에 관한 잘못된 지식도 넘쳐납니다.  
> 이 앱은 **객관적인 정보**를 제공하고, **토론의 장을 열어**  
> 더 정확하고 올바른 건강 정보를 유도하기 위해 제작되었습니다.

---

## 🚀 주요 기능

- 📋 **게시판을 이용한 사용자 소통** (진행중)
- 📚 **Health 관련 논문 자료 제공** (진행 예정)
- 💡 **헬스 관련 유용한 정보 아카이빙** (진행 예정)
- 🛒 **쇼핑몰 기능 (영양제, 도구 등)** (진행 예정)

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

- Spring Security + JWT 기반 인증 & 권한 부여 구현
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
- **Elasticsearch 기반 고속 검색 기능** 구현
- Batch를 통한 스케줄링 구현

---

## 📦 아키텍처
![architecture](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FraIe1%2FbtsNh4PrxeT%2FEAy6ke9IwkyzvTudzVJbF0%2Fimg.png)

## 🎨 UI
![회원가입](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fcb2G1s%2FbtsNip6RW0I%2Fkc39YE0Yv7e5UyDrgEJYbk%2Fimg.png)
![로그인](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FF9Wmb%2FbtsNG1jwtUB%2FAkYIXfHKSSA19negRJYnX0%2Fimg.png)
![메인화면](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbIPUvc%2FbtsNg9h6HaK%2F363cQOtZRUt4ousif5ynN0%2Fimg.png)
![글작성](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FoymCN%2FbtsNiATveEy%2FmjouMkFHdgGvF7KPFLEBMk%2Fimg.png)
![게시글보기](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FR4k8c%2FbtsNjAMrbYE%2FhTuS7rmxymgtAqqcCzktk0%2Fimg.png)
![댓글](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FEgh3P%2FbtsNisPJr1B%2FM2X7kXBLlZCmXYjlgka5BK%2Fimg.png)