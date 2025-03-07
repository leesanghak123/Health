<template>
  <div class="d-flex justify-content-center align-items-center" style="min-height: 80vh;">
    <div class="card p-4" style="width: 400px;">
      <h3 class="card-title text-center mb-4">로그인</h3>
      <form @submit.prevent="login">
        <div class="form-group">
          <input type="text" v-model="username" class="form-control" placeholder="아이디" required />
        </div>
        <div class="form-group">
          <input type="password" v-model="password" class="form-control" placeholder="비밀번호" required />
        </div>
        <div class="text-end">
          <button type="submit" class="btn btn-primary w-100">로그인</button>
        </div>
        <p v-if="errorMessage" class="text-danger mt-2">{{ errorMessage }}</p>
      </form>
      <div class="d-flex justify-content-between mt-3">
        <a @click="handleSocialLogin" href="http://localhost:8002/oauth2/authorization/naver" class="btn btn-outline-success w-48">네이버 로그인</a>
        <a @click="handleSocialLogin" href="http://localhost:8002/oauth2/authorization/google" class="btn btn-outline-danger w-48">구글 로그인</a>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
export default {
  data() {
    return {
      username: '',
      password: '',
      errorMessage: '',
    };
  },
  mounted() {
    // URL에 소셜 로그인 파라미터가 있거나 세션 스토리지에 표시가 있는 경우에만 실행
    const urlParams = new URLSearchParams(window.location.search);
    const isFromSocialLogin = urlParams.get('social') === 'true' || sessionStorage.getItem('fromSocialLogin');
    
    if (isFromSocialLogin) {
      // 소셜 로그인 표시 제거
      sessionStorage.removeItem('fromSocialLogin');
      // URL 파라미터 정리 (선택사항)
      if (urlParams.get('social')) {
        const newUrl = window.location.pathname;
        window.history.replaceState({}, document.title, newUrl);
      }
      // 쿠키 확인
      this.getJWTFromCookie();
    }
  },
  methods: {
    // 소셜 로그인 버튼 클릭 처리
    handleSocialLogin() {
      // 소셜 로그인 진행 중임을 표시
      sessionStorage.setItem('fromSocialLogin', 'true');
    },
    
    // 일반 로그인 (JWT를 로컬 스토리지에 저장)
    login() {
      axios.post('http://localhost:8002/login', {
        username: this.username,
        password: this.password
      })
      .then(response => {
        const token = response.headers['authorization'];
        if (token) {
          const jwt = token.split(' ')[1];
          localStorage.setItem('jwt', jwt);
          axios.defaults.headers.common['Authorization'] = `Bearer ${jwt}`;
          this.$router.push('/');
        } else {
          this.errorMessage = '로그인 중 오류가 발생했습니다.';
        }
      })
      .catch(() => {
        this.errorMessage = '아이디 또는 비밀번호가 잘못되었습니다.';
      });
    },

    // 소셜 로그인 후 쿠키 기반으로 JWT 요청
    getJWTFromCookie() {
      // Body는 null, 쿠키는 포함: withCredentials: true
      axios.post('http://localhost:8002/api/auth/jwt', {}, { withCredentials: true })
      .then(response => {
        const jwt = response.data.jwt;
        if (jwt) {
          localStorage.setItem('jwt', jwt);
          axios.defaults.headers.common['Authorization'] = `Bearer ${jwt}`;
          this.$router.push('/');
        }
      })
      .catch(error => {
        console.error('소셜 로그인 JWT 처리 오류:', error);
        this.errorMessage = '소셜 로그인 처리 중 오류가 발생했습니다.';
      });
    }
  }
};
</script>

<style scoped>
.form-group {
  margin-bottom: 20px;
}

.btn {
  width: 48%;
}
</style>