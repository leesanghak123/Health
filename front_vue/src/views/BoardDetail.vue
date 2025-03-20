<template>
  <div class="board-detail-container">
    <h2 class="board-detail-title">{{ board.title }}</h2>

    <div class="board-detail-meta">
      <p>작성자: {{ truncateUsername(board.username) }}</p>
      <p>작성일: {{ formatDate(board.createDate) }}</p>
      <p>조회수: {{ board.count }}</p>
      <p>추천수: {{ board.likeCnt }}</p>
      
      <button @click="toggleLike" class="like-button">
        <span v-if="board.liked">❤️</span>
        <span v-else>🤍</span>
        {{ board.likeCnt }}
      </button>

      <div v-if="isAuthor" class="author-actions">
        <button @click="editPost" class="btn-toggle">수정</button>
        <button @click="deletePost" class="btn-toggle">삭제</button>
      </div>
    </div>

    <div class="board-detail-content">
      <div v-html="board.content"></div>
    </div>

    <div class="button-group">
      <button @click="goToList" class="btn btn-list">목록</button>
    </div>
  </div>
</template>

<script>
import apiClient from "@/services/reissue";

export default {
  data() {
    return {
      board: {
        id: null,
        title: '',
        content: '',
        username: '',
        createDate: '',
        count: 0,
        likeCnt: 0,
        liked: false
      },
      isAuthor: false
    };
  },
  methods: {
    async fetchBoard() {
      try {
        const token = localStorage.getItem("access");
        const response = await apiClient.get(`/board/${this.$route.params.id}`, {
          headers: {
            access: token,
          }
        });

        this.board = response.data;
        console.log("불러온 게시글 데이터:", this.board);

        // JWT 디코딩하여 작성자인지 확인
        const parts = token.split(".");
        if (parts.length === 3) {
          const payload = JSON.parse(atob(parts[1]));
          const currentUser = payload.username;
          this.isAuthor = this.board.username === currentUser;
        } else {
          console.error("JWT 형식이 잘못되었습니다.");
        }
      } catch (error) {
        console.error("게시글 불러오기 실패:", error);
      }
    },

    truncateUsername(username) {
      if (username && username.length > 5) {
        return username.substring(0, 5) + "...";
      }
      return username;
    },

    formatDate(dateString) {
      const options = { year: "numeric", month: "2-digit", day: "2-digit" };
      return new Date(dateString).toLocaleDateString("ko-KR", options);
    },

    goToList() {
      this.$router.push("/");
    },

    editPost() {
      this.$router.push(`/board/update/${this.board.id}`);
    },

    // 좋아요 토글 기능 (추가 또는 삭제)
    async toggleLike() {
      try {
        const token = localStorage.getItem("access");
        
        if (this.board.liked) {
          // 좋아요가 이미 되어있으면 삭제 요청
          await apiClient.delete(`/board/${this.board.id}/like`, {
            headers: {
              access: token,
            }
          });
          
          // 좋아요 상태 및 카운트 업데이트
          this.board.liked = false;
          this.board.likeCnt -= 1; // 좋아요 수 감소
        } else {
          // 좋아요가 안 되어있으면 추가 요청
          await apiClient.post(`/board/${this.board.id}/like`, {}, {
            headers: {
              access: token,
            }
          });
          
          // 좋아요 상태 및 카운트 업데이트
          this.board.liked = true;
          this.board.likeCnt += 1; // 좋아요 수 증가
        }
        
      } catch (error) {
        console.error("좋아요 처리 실패:", error);
        alert("좋아요 처리 중 오류가 발생했습니다.");
        this.fetchBoard(); // 오류 발생 시 데이터 재동기화
      }
    },

    async deletePost() {
      if (confirm("정말 삭제하시겠습니까?")) {
        try {
          const token = localStorage.getItem("access");
          await apiClient.delete(`/board/delete/${this.board.id}`, {
            headers: {
              access: token,
            },
          });

          alert("게시글이 삭제되었습니다.");
          this.$router.push("/");
        } catch (error) {
          console.error("게시글 삭제 실패:", error);
          alert("게시글 삭제 중 오류가 발생했습니다.");
        }
      }
    },
  },
  mounted() {
    this.fetchBoard();
  },
};
</script>
  
  <style scoped>
  .board-detail-container {
    width: 100%;
    max-width: 100%;
    min-height: 100vh;
    margin: 0;
    padding: 20px;
    background-color: #f9f9f9;
    box-sizing: border-box;
  }
  
  .board-detail-title {
    font-size: 1.8rem;
    font-weight: bold;
    text-align: center;
    margin-bottom: 20px;
    color: #333;
  }
  
  .board-detail-meta {
    font-size: 0.9rem;
    color: #555;
    margin-bottom: 20px;
    border-bottom: 1px solid #ddd;
    padding-bottom: 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
  }
  
  .board-detail-meta p {
    margin: 5px 0;
  }
  
  .board-detail-content {
    width: 100%;
    min-height: 60vh;
    padding: 15px;
    background-color: #fff;
    border: 1px solid #ddd;
    border-radius: 4px;
    overflow-y: auto;
    box-sizing: border-box;
  }
  
  .button-group {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
  
  button {
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: background-color 0.3s ease;
  }
  
  button:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
  
  .like-button {
    background-color: transparent;
    padding: 5px 10px;
  }
  
  button.btn-list {
    background-color: #ccc;
    color: black;
    padding: 10px 30px;
  }
  
  button.btn-list:hover {
    background-color: #bbb;
  }
  
  .author-actions {
    display: flex;
    gap: 10px;
    align-items: center;
  }
  
  .btn-toggle {
    padding: 5px 10px;
    background-color: #f0f0f0;
    border: 1px solid #ddd;
    border-radius: 4px;
    color: #333;
    font-size: 12px;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .btn-toggle:hover {
    background-color: #e0e0e0;
  }
  
  .btn-toggle:first-child {
    border-color: #007bff;
    color: #007bff;
  }
  
  .btn-toggle:first-child:hover {
    background-color: #007bff;
    color: white;
  }
  
  .btn-toggle:last-child {
    border-color: #dc3545;
    color: #dc3545;
  }
  
  .btn-toggle:last-child:hover {
    background-color: #dc3545;
    color: white;
  }
  
  @media (max-width: 768px) {
    .board-detail-meta {
      flex-direction: column;
      align-items: flex-start;
    }
  
    .author-actions {
      margin-top: 10px;
      align-self: flex-end;
    }
  }
  </style>