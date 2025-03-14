import axios from 'axios';
import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Join from '../views/Join.vue'
import Board from '../views/Board.vue'
import BoardWrite from '../views/BoardWrite.vue'
import BoardDetail from '../views/BoardDetail.vue';
import BoardUpdate from '../views/BoardUpdate.vue';
import { setupAxiosDefaults } from '../services/reissue';

// 페이지 경로 상수
const LOGIN_PATH = '/login';
const JOIN_PATH = '/join';
const BOARD_PATH = '/';
const BOARD_WRITE_PATH = '/board/write'
const BOARD_DETAIL_PATH = '/board/detail'
const BOARD_UPDATE_PATH = '/board/update'

const routes = [
  {
    path: BOARD_PATH,
    name: 'board',
    component: Board,
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
  {
  path: BOARD_WRITE_PATH,
  name: 'boardWrite',
  component: BoardWrite,
  meta: { requiresAuth: true }
  },
  {
  path: `${BOARD_DETAIL_PATH}/:id`,
  name: 'boardDetail',
  component: BoardDetail,
  meta: { requiresAuth: true }
  },
  {
    path: `${BOARD_UPDATE_PATH}/:id`,
    name: 'boardUpdate',
    component: BoardUpdate,
    meta: { requiresAuth: true }
  },
]

// 라우터 인스턴스 생성 (페이지 전환)
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
      setupAxiosDefaults(); // reissue (access 가져오기 등)
      next();
    }
  } else {
    next();
  }
});

export default router;