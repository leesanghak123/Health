<template>
    <div class="d-flex justify-content-center align-items-center" style="min-height: 80vh;">
      <div class="card p-4" style="width: 400px;">
        <h3 class="card-title text-center mb-4">회원가입</h3>
        <form @submit.prevent="register">
          <div class="form-group">
            <input type="text" v-model="username" class="form-control" placeholder="아이디" id="username" autocomplete="username" required />
          </div>
          <div class="form-group">
            <input type="password" v-model="password" class="form-control" placeholder="비밀번호" id="password" autocomplete="current-password" required />
          </div>
          <div class="form-group">
            <input type="email" v-model="email" class="form-control" placeholder="이메일" id="email" autocomplete="email" required />
          </div>
          <div class="text-end"> <!-- 오른쪽 정렬 -->
            <button type="submit" id="btn-save" class="btn btn-success w-100">가입하기</button>
          </div>
          <p v-if="errorMessage" class="text-danger mt-2">{{ errorMessage }}</p>
        </form>
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
        name: '',
        email: '',
        errorMessage: '',
      };
    },
    methods: {
      register() {
        axios.post(`${process.env.VUE_APP_API_BASE_URL}/api/auth/join`, {
          username: this.username,
          password: this.password,
          email: this.email
        })
        .then(response => {
          console.log('회원가입 성공:', response.data);
          this.$router.push('/login');
        })
        .catch(error => {
          this.errorMessage = '회원가입 중 오류가 발생했습니다.';
          console.error('회원가입 실패:', error);
        });
      }
    }
  };
  </script>
  
  <style scoped>
  .form-group {
    margin-bottom: 20px;
  }
  </style>