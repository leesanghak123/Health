<template>
  <div class="d-flex justify-content-center align-items-center" style="min-height: 80vh;">
    <div class="card p-4" style="width: 400px;">
      <h3 class="card-title text-center mb-4">로그인</h3>
      <form @submit.prevent="login">
        <div class="form-group">
          <input type="text" v-model="username" class="form-control" placeholder="아이디" autocomplete="username" required />
        </div>
        <div class="form-group">
          <input type="password" v-model="password" class="form-control" placeholder="비밀번호" autocomplete="current-password" required />
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
import apiClient from '@/services/reissue';

export default {
  data() {
    return {
      username: '',
      password: '',
      errorMessage: '',
    };
  },
  mounted() {
    // URL에서 social 파라미터 확인 후 소셜 로그인 처리
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('social') === 'true') {
      this.getJWTFromRefreshToken();
      // URL 파라미터 제거
      const newUrl = window.location.pathname;
      window.history.replaceState({}, document.title, newUrl);
    }
  },
  methods: {    
    // 일반 로그인
    login() {
      // 입력값 검증
      if (!this.username || !this.password) {
        this.errorMessage = '아이디와 비밀번호를 모두 입력해주세요.';
        return;
      }
       
      apiClient.post('/login', {
        username: this.username,
        password: this.password
      }, { withCredentials: true })  // 로그인 시 쿠키 포함 (Refresh 토큰 저장 목적)
      .then(response => {
        const token = response.headers['access'];
        
        if (token) {
          console.log('유효한 토큰이 있음, 저장 및 헤더 설정');
          // 토큰 저장 (Bearer 접두사 없이)
          localStorage.setItem('access', token);
          // 서버 요청 시 access 헤더 사용
          axios.defaults.headers.common['access'] = token;
          window.location.href = '/'; // 새로고침
        } else {
          this.errorMessage = '로그인 중 오류가 발생했습니다.';
        }
      })
      .catch(error => {
        this.errorMessage = '아이디 또는 비밀번호가 잘못되었습니다.';
      });
    },

    // 소셜 로그인 후 쿠키 기반으로 JWT 요청
    getJWTFromRefreshToken() {
      axios.get(`${process.env.VUE_APP_API_BASE_URL}/api/auth/jwt`, { withCredentials: true })
      .then(response => {
        const accessToken = response.headers['access'];
        if (accessToken) {
          localStorage.setItem('access', accessToken);
          axios.defaults.headers.common['access'] = accessToken;
          window.location.href = '/'; // 새로고침
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