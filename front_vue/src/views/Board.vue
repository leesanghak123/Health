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
import apiClient from '@/services/reissue';

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

    apiClient.get('/')
      .then(response => {
        this.data = response.data;
      })
      .catch(error => {
        console.error("데이터 불러오기 실패:", error);
        this.error = '데이터를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
      })
      .finally(() => {
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