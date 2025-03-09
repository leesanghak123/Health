import axios from 'axios';
import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Join from '../views/Join.vue'
import Board from '../views/Board.vue'
import { setupAxiosDefaults } from '../services/reissue'; // 경로 수정
// 페이지 경로 상수
const LOGIN_PATH = '/login';
const JOIN_PATH = '/join';
const BOARD_PATH = '/';

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

// 라우터 인스턴스 생성
export const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 라우터 전환 시 인증 여부 확인
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('access');

  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) {
      // 토큰이 없는 경우 로그인으로 리다이렉트
      localStorage.removeItem('access');
      delete axios.defaults.headers.common['access'];
      next(LOGIN_PATH);
    } else {
      // 토큰이 있으면 axios 설정 업데이트하고 통과
      setupAxiosDefaults(); // 공통 함수 사용
      next();
    }
  } else {
    next();
  }
});

export default router;