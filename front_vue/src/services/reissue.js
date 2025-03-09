import axios from 'axios';
import { router } from '../router'; // 라우터 인스턴스 import

const apiClient = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8002',
  withCredentials: true,  // 중요! 쿠키 포함 설정
});

// 공통으로 사용할 axios 설정 함수
export const setupAxiosDefaults = () => {
  const token = localStorage.getItem('access');
  if (token) {
    // 하나의 방식으로 설정
    axios.defaults.headers.common['access'] = token;
    apiClient.defaults.headers.common['access'] = token;
  }
};

apiClient.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    // 더 자세한 에러 로깅
    console.log("에러 상세 정보:", {
      status: error.response?.status,
      data: error.response?.data,
      dataType: typeof error.response?.data,
      contentType: error.response?.headers['content-type'],
      stringData: typeof error.response?.data === 'string' ? error.response?.data : null,
      url: originalRequest.url
    });
    
    // 401 에러이고 TOKEN_EXPIRED 에러인 경우 토큰 재발급 시도
    // 문자열 응답을 처리하기 위한 조건 추가
    if (
      error.response?.status === 401 && 
      (
        (error.response?.data?.error === 'TOKEN_EXPIRED') || 
        (typeof error.response?.data === 'string' && error.response?.data.includes('TOKEN_EXPIRED'))
      ) && 
      !originalRequest._retry
    ) {
      originalRequest._retry = true;
      
      try {
        // /reissue 엔드포인트 호출
        const response = await apiClient.post('/reissue');
        
        console.log("토큰 재발급 응답:", response);

        // 응답 헤더에서 새 액세스 토큰 가져오기
        const newAccessToken = response.headers.access;
        console.log("🔑 새 액세스 토큰:", newAccessToken);
        
        if (newAccessToken) {
          // 로컬 스토리지에 저장
          localStorage.setItem('access', newAccessToken);
          
          // axios 기본 헤더 업데이트
          setupAxiosDefaults();
          
          // 원래 요청의 헤더 업데이트
          originalRequest.headers.access = newAccessToken;
          
          // 실패했던 요청 재시도
          return apiClient(originalRequest);
        } else {
          router.push('/login'); // 라우터 사용
          return Promise.reject(new Error('No access token in response'));
        }
      } catch (refreshError) {
        console.log("에러 상세:", refreshError.response?.status, refreshError.response?.data);
        
        // 리프레시 토큰도 만료된 경우 로그인 페이지로 리다이렉트
        router.push('/login'); // 라우터 사용
        
        // Error 객체로 변환하여 반환
        if (refreshError instanceof Error) {
          return Promise.reject(refreshError);
        } else {
          return Promise.reject(new Error(refreshError?.message || 'Token refresh failed'));
        }
      }
    }
    
    // 다른 모든 에러도 Error 객체로 확인
    if (error instanceof Error) {
      return Promise.reject(error);
    } else {
      return Promise.reject(new Error('An error occurred during the request'));
    }
  }
);

// 요청 인터셉터: 모든 요청에 액세스 토큰 추가
apiClient.interceptors.request.use(
  config => {
    // 로컬 스토리지에서 액세스 토큰 가져오기
    const accessToken = localStorage.getItem('access');
    if (accessToken) {
        config.headers.access = accessToken;
    }
    return config;
  },
  error => {
    return Promise.reject(error instanceof Error ? error : new Error('Request configuration error'));
  }
);

export default apiClient;