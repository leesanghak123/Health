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

      
      <!-- 소셜 로그인 이미지 버튼 -->
      <div class="d-flex justify-content-between mt-3">
        <a @click="handleSocialLogin('naver')" class="w-48">
          <img :src="naverImg" alt="네이버 로그인" class="img-fluid" />
        </a>
        <a @click="handleSocialLogin('google')" class="w-48">
          <img :src="googleImg" alt="구글 로그인" class="img-fluid" />
        </a>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import apiClient from '@/services/reissue'
import naverImg from '@/assets/naver_login_btn.png'
import googleImg from '@/assets/google_login_btn.png'

export default {
  data() {
    return {
      username: '',
      password: '',
      errorMessage: '',
      naverImg,
      googleImg,
    };
  },
  mounted() {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('social') === 'true') {
      this.getJWTFromRefreshToken();
      const newUrl = window.location.pathname;
      window.history.replaceState({}, document.title, newUrl);
    }
  },
  methods: {
    login() {
      if (!this.username || !this.password) {
        this.errorMessage = '아이디와 비밀번호를 모두 입력해주세요.';
        return;
      }

      apiClient.post('/login', {
        username: this.username,
        password: this.password
      }, { withCredentials: true })
      .then(response => {
        const token = response.headers['access'];
        if (token) {
          localStorage.setItem('access', token);
          axios.defaults.headers.common['access'] = token;
          window.location.href = '/';
        } else {
          this.errorMessage = '로그인 중 오류가 발생했습니다.';
        }
      })
      .catch(() => {
        this.errorMessage = '아이디 또는 비밀번호가 잘못되었습니다.';
      });
    },

    handleSocialLogin(provider) {
      const url = `http://localhost:8002/oauth2/authorization/${provider}`
      window.location.href = url
    },

    getJWTFromRefreshToken() {
      axios.get(`${process.env.VUE_APP_API_BASE_URL}/api/auth/jwt`, { withCredentials: true })
      .then(response => {
        const accessToken = response.headers['access'];
        if (accessToken) {
          localStorage.setItem('access', accessToken);
          axios.defaults.headers.common['access'] = accessToken;
          window.location.href = '/';
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
.w-48 {
  width: 48%;
}
.img-fluid {
  width: 100%;
  height: 90%;
  cursor: pointer;
}
</style>