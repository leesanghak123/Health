<!-- App.vue -->
<template>
  <div id="app">
    <nav class="navbar navbar-expand-md navbar-light bg-light">
      <div class="container-fluid">
        <a class="navbar-brand" href="/">Health</a>
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
      isLoggedIn: !!localStorage.getItem('jwt'),
    };
  },
  methods: {
    logout() {
      localStorage.removeItem('jwt');
      delete axios.defaults.headers.common['Authorization'];
      this.isLoggedIn = false;
      this.$router.push('/login');
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