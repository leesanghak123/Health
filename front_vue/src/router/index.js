import axios from 'axios';
import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Join from '../views/Join.vue'
import Board from '../views/Board.vue'

// 페이지 경로 상수
const LOGIN_PATH = '/login';
const JOIN_PATH = '/join';
const BOARD_PATH = '/';

// JWT 만료 여부 확인
function isTokenExpired(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1])); // JWT의 페이로드를 디코딩
    const expiration = payload.exp; // exp 시간 (초 단위)
    const now = Math.floor(new Date().getTime() / 1000); // 현재 시간 (밀리초 -> 초)
    return now > expiration; // 현재 시간이 만료 시간을 초과하는지 확인
  }catch (error) {
    console.error('Error decoding token:', error);
    return true; // 디코딩에 실패하면 토큰이 만료된 것으로 간주
  }
}

const routes = [
  {
    path: BOARD_PATH,
    name: 'board',
    component: Board,
    meta: { requiresAuth: true }
  },
  {
    path: LOGIN_PATH,
    name: 'login',
    component: Login
  },
  {
    path: JOIN_PATH,
    name: 'join',
    component: Join
  },
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL), // 새로고침 없이 URL 변경
  routes
})

// 라우터 전환 시 인증 여부 확인 (인증이 안되면 초기화)
router.beforeEach((to, from, next) => { // beforeEach : 라우터가 변경되기 전, to:이동하려는 라우트 객체, from:현재 라우트 객체
  const token = localStorage.getItem('jwt');

  if (to.matched.some(record => record.meta.requiresAuth)) { // requiresAuth 메타데이터가 있는 경우
    if (!token || isTokenExpired(token)) {
      localStorage.removeItem('jwt');
      delete axios.defaults.headers.common['Authorization']; // 헤더 초기화
      next(LOGIN_PATH); // 로그인 페이지
    } else {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`; // JWT 설정
      next();
    }
  } else {  // 인증이 필요하지 않은 경우
    next();
  }
});

export default router