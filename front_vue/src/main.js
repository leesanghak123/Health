// Vue를 초기화하고 필요한 설정을 적용하는 부분
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import apiClient from './services/reissue';

import $ from 'jquery';
window.$ = window.jQuery = $;
import 'bootstrap/dist/css/bootstrap.min.css'; // 부트스트랩 CSS
import 'bootstrap/dist/js/bootstrap.bundle.min.js'; // 부트스트랩 JavaScript
import axios from "axios";

// JWT 토큰을 기본 헤더에 설정
const token = localStorage.getItem('access');
if (token) { // 토큰이 있다면 Axios 헤더에 JWT 토큰을 포함
    // (Authorization 대신 access 헤더 사용 및 Bearer 접두사 제거)
    axios.defaults.headers.common['access'] = token;
}


// app 인스턴스 생성
const app = createApp(App);

// apiClient(커스텀 Axios 인스턴스)를 전역 속성으로 등록
app.config.globalProperties.$http = apiClient;

// router 등록 후 마운트
app.use(router).mount('#app')