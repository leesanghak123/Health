<template>
  <div class="container">
    <h1 class="text-center">Success</h1>
    <div class="card mt-4">
      <div class="card-body">
        <h2 class="card-title">응답 결과</h2>
        <p class="card-text">{{ data }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from '@/services/reissue';  // apiClient 추가

export default {
  data() {
    return {
      loading: true,
      data: '',
      error: null
    };
  },
  mounted() {
    this.fetchSuccessData();
  },
  methods: {
    fetchSuccessData() {
    console.log("📡 fetchSuccessData 실행됨!"); // ✅ 함수 실행 확인

    //axios.get('http://localhost:8002/')
    apiClient.get('/')
      .then(response => {
        console.log("✅ 서버 응답 받음:", response.data); // ✅ 응답 데이터 확인
        this.data = response.data;
      })
      .catch(error => {
        console.error("데이터 불러오기 실패:", error);
        console.log("에러 상세:", {
          status: error.response?.status,
          data: error.response?.data,
          headers: error.response?.headers,
          url: error.config?.url
        });
        this.error = '데이터를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
      })
      .finally(() => {
        console.log("🔄 요청 완료됨, loading 상태 변경"); // ✅ 로딩 상태 확인
        this.loading = false;
      });
  }
  }
};
</script>

<style scoped>
.container {
  max-width: 800px;
  margin-top: 2rem;
}
</style>