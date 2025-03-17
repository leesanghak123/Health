<template>
  <div id="app">
    <nav class="navbar navbar-expand-md navbar-light bg-light">
      <div class="container-fluid">
        <a class="navbar-brand" href="/">Health</a>

        <!-- Vue에서 직접 상태 관리하는 토글 버튼 -->
        <button class="navbar-toggler" @click="toggleNavbar">
          <span class="navbar-toggler-icon"></span>
        </button>

        <!-- Vue 상태를 활용하여 `show` 클래스 적용 -->
        <div class="collapse navbar-collapse" :class="{ show: isNavOpen }" id="collapsibleNavbar">
          <ul class="navbar-nav ms-auto align-items-center">
            <li v-if="!isLoggedIn" class="nav-item">
              <router-link to="/login" class="nav-link">로그인</router-link>
            </li>
            <li v-if="!isLoggedIn" class="nav-item">
              <router-link to="/join" class="nav-link">회원가입</router-link>
            </li>
            <li v-else class="nav-item">
              <router-link to="#" class="nav-link" @click="logout">로그아웃</router-link>
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
import { Collapse } from 'bootstrap';
import axios from 'axios';

export default {
  name: 'App',
  data() {
    return {
      isLoggedIn: !!localStorage.getItem('access'),
      isNavOpen: false, // 네비게이션 바 상태 관리
    };
  },
  mounted() {
    // Bootstrap Collapse 기능 수동 초기화
    this.navbarCollapse = new Collapse(document.getElementById('collapsibleNavbar'), {
      toggle: false, // 자동으로 열리는 것을 방지
    });
  },
  methods: {
    toggleNavbar() {
      this.isNavOpen = !this.isNavOpen;
    },
    logout() {
      this.$http.post('/logout', {}, { withCredentials: true })
        .then(() => {
          localStorage.removeItem('access');
          delete axios.defaults.headers.common['access'];
          this.isLoggedIn = false;
          window.location.href = '/login';
        })
        .catch(error => {
          console.error('로그아웃 중 오류 발생:', error);
          localStorage.removeItem('access');
          delete axios.defaults.headers.common['access'];
          this.isLoggedIn = false;
          window.location.href = '/login';
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

/* 네비게이션 애니메이션 */
.collapse {
  transition: height 0.3s ease;
}
</style>