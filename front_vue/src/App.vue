<template>
  <div id="app">
    <nav class="navbar navbar-expand-md navbar-light bg-light">
      <div class="container-fluid">
        <a class="navbar-brand" href="/">Health</a>
        <!-- 반응형 nav -->
        <button
          class="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#collapsibleNavbar"
          aria-controls="collapsibleNavbar"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="collapsibleNavbar">
          <ul class="navbar-nav ms-auto align-items-center">
            <li v-if="!isLoggedIn" class="nav-item">
              <router-link to="/login" class="nav-link">로그인</router-link>
            </li>
            <li v-if="!isLoggedIn" class="nav-item">
              <router-link to="/join" class="nav-link">회원가입</router-link>
            </li>
            <li v-else class="nav-item">
              <router-link to="/" class="nav-link" @click="logout">로그아웃</router-link>
            </li>
          </ul>
        </div>
      </div>
    </nav>
    <router-view />
    <footer class="footer mt-auto py-3 bg-light text-dark">
      <div class="container text-center">
        <span class="text-muted">Create by Sanghak | 📞010-0000-0000</span>
      </div>
    </footer>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'App',
  data() {
    return {
      isLoggedIn: !!localStorage.getItem('access'),
    };
  },
  methods: {
    logout() {
    // 백엔드 로그아웃 API 호출 (리프레시 토큰은 쿠키에 있으므로 별도로 전송할 필요 없음)
    this.$http.post('/logout', {}, { withCredentials: true })  // Refresh tokrn 삭제 땜시
      .then(() => {
        // 성공적으로 로그아웃된 경우
        localStorage.removeItem('access');
        delete axios.defaults.headers.common['access'];
        this.isLoggedIn = false;
        this.$router.push('/login');
      })
      .catch(error => {
        console.error('로그아웃 중 오류 발생:', error);
        // 오류가 발생해도 클라이언트 측에서는 토큰을 삭제하고 로그인 페이지로 이동
        localStorage.removeItem('access');
        delete axios.defaults.headers.common['access'];
        this.isLoggedIn = false;
        this.$router.push('/login');
      });
  },
  },
};
</script>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.footer {
  font-size: 0.8em;
  color: #6c757d; /* 텍스트 색상 조정 */
}
</style>