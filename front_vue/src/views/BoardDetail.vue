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

      <div v-if="isAuthor" class="author-badges">
        <button @click="editPost" class="badge badge-edit">수정</button>
        <button @click="deletePost" class="badge badge-delete">삭제</button>
      </div>
    </div>

    <div class="board-detail-content">
      <div v-html="board.content"></div>
    </div>

    <!-- 댓글 입력창 -->
    <div class="comment-section">
      <h5>댓글</h5>
      <div class="comment-input">
        <textarea v-model="newReply" placeholder="댓글을 입력하세요"></textarea>
        <button @click="postComment">등록</button>
      </div>

      <!-- 댓글 목록 -->
      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <p class="comment-content">{{ comment.content }}</p>
          <p class="comment-meta">
            {{ comment.username }} · {{ formatDate(comment.createDate) }}
            <span @click="toggleReplyInput(comment.id)" class="reply-btn text-primary">답글</span>
            <span v-if="isCommentAuthor(comment)" @click="deleteComment(comment.id)" class="delete-btn text-danger">삭제</span>
          </p>

          <!-- 대댓글 입력창 -->
          <div v-if="activeReplyInput === comment.id" class="reply-input">
            <textarea v-model="newSubReply" placeholder="대댓글을 입력하세요"></textarea>
            <button @click="postSubReply(comment.id)">등록</button>
          </div>

          <!-- 대댓글 목록 -->
          <div v-for="subReply in getSubRepliesForComment(comment.id)" :key="subReply.id" class="reply-comment-item">
            <p class="comment-content">{{ subReply.content }}</p>
            <p class="comment-meta">
              {{ subReply.username }} · {{ formatDate(subReply.createDate) }}
              <span v-if="isCommentAuthor(subReply)" @click="deleteComment(subReply.id, true)" class="delete-btn">삭제</span>
            </p>
          </div>
        </div>
      </div>
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
      comments: [],
      subReplies: [],
      newReply: "",
      newSubReply: "",
      activeReplyInput: null,
      isAuthor: false
    };
  },
  methods: {
    async fetchBoard() {
      try {
        const token = localStorage.getItem("access");
        const response = await apiClient.get(`/board/${this.$route.params.id}`, {
          headers: { access: token }
        });

        // 기본 게시글 정보 설정
        this.board = {
          id: response.data.id,
          title: response.data.title,
          content: response.data.content,
          username: response.data.username,
          createDate: response.data.createDate,
          count: response.data.count,
          likeCnt: response.data.likeCnt,
          liked: response.data.liked
        };

        // 댓글과 대댓글 설정
        this.comments = response.data.reply || [];
        this.subReplies = response.data.reReply || [];
        
        console.log("불러온 게시글 데이터:", this.board);
        console.log("댓글 데이터:", this.comments);
        console.log("대댓글 데이터:", this.subReplies);

        // 작성자 확인
        this.isAuthor = this.board.username === this.getUsernameFromToken();
      } catch (error) {
        console.error("게시글 불러오기 실패:", error);
        alert("게시글을 불러오는 중 오류가 발생했습니다.");
      }
    },

    async postComment() {
      if (!this.newReply.trim()) {
        alert("댓글을 입력하세요.");
        return;
      }

      try {
        const token = localStorage.getItem("access");
        await apiClient.post(
          `/reply`,
          { 
            boardid: this.board.id, 
            content: this.newReply 
          },
          { headers: { access: token } }
        );

        this.newReply = "";
        this.fetchBoard();
      } catch (error) {
        console.error("댓글 작성 실패:", error);
        alert("댓글 작성 중 오류가 발생했습니다.");
      }
    },

    async postSubReply(commentId) {
      if (!this.newSubReply.trim()) {
        alert("대댓글을 입력하세요.");
        return;
      }

      try {
        const token = localStorage.getItem("access");
        await apiClient.post(
          `/rereply`,
          { 
            replyid: commentId, 
            content: this.newSubReply 
          },
          { headers: { access: token } }
        );

        this.newSubReply = "";
        this.activeReplyInput = null;
        this.fetchBoard();
      } catch (error) {
        console.error("대댓글 작성 실패:", error);
        alert("대댓글 작성 중 오류가 발생했습니다.");
      }
    },

    async deleteComment(commentId, isSubReply = false) {
      if (!confirm("정말 삭제하시겠습니까?")) return;

      try {
        const token = localStorage.getItem("access");
        const url = isSubReply ? `/rereply/delete/${commentId}` : `/reply/delete/${commentId}`;

        await apiClient.delete(url, {
          headers: { access: token },
        });

        this.fetchBoard();
      } catch (error) {
        console.error("댓글 삭제 실패:", error);
        alert("댓글 삭제 중 오류가 발생했습니다.");
      }
    },

    toggleReplyInput(commentId) {
      this.activeReplyInput = this.activeReplyInput === commentId ? null : commentId;
    },

    getUsernameFromToken() {
      const token = localStorage.getItem("access");
      if (!token) return null;
      
      try {
        const parts = token.split(".");
        if (parts.length === 3) {
          const payload = JSON.parse(atob(parts[1]));
          return payload.username;
        }
      } catch (error) {
        console.error("토큰 파싱 오류:", error);
      }
      
      return null;
    },

    isCommentAuthor(comment) {
      const username = this.getUsernameFromToken();
      return comment.username === username;
    },

    formatDate(dateString) {
      if (!dateString) return '';
      
      const options = { year: "numeric", month: "2-digit", day: "2-digit" };
      return new Date(dateString).toLocaleDateString("ko-KR", options);
    },

    truncateUsername(username) {
      if (!username) return '';
      
      return username.length > 10 ? username.substring(0, 10) + '...' : username;
    },

    goToList() {
      this.$router.push("/");
    },

    editPost() {
      this.$router.push(`/board/update/${this.board.id}`);
    },

    async toggleLike() {
      try {
        const token = localStorage.getItem("access");

        if (this.board.liked) {
          await apiClient.delete(`/board/${this.board.id}/like`, { headers: { access: token } });
          this.board.liked = false;
          this.board.likeCnt -= 1;
        } else {
          await apiClient.post(`/board/${this.board.id}/like`, {}, { headers: { access: token } });
          this.board.liked = true;
          this.board.likeCnt += 1;
        }
      } catch (error) {
        console.error("좋아요 처리 실패:", error);
        alert("좋아요 처리 중 오류가 발생했습니다.");
        this.fetchBoard();
      }
    },

    async deletePost() {
      if (!confirm("정말 삭제하시겠습니까?")) return;

      try {
        const token = localStorage.getItem("access");
        await apiClient.delete(`/board/delete/${this.board.id}`, { headers: { access: token } });

        alert("게시글이 삭제되었습니다.");
        this.$router.push("/");
      } catch (error) {
        console.error("게시글 삭제 실패:", error);
        alert("게시글 삭제 중 오류가 발생했습니다.");
      }
    },

    getSubRepliesForComment(commentId) {
      return this.subReplies.filter(reply => reply.replyid === commentId);
    }
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

.comment-section {
  margin-top: 30px;
  max-width: 95%;
  margin-left: auto;
  margin-right: auto;
}

.comment-input {
  display: flex;
  margin-bottom: 20px;
}

.comment-input textarea {
  flex-grow: 1;
  height: 60px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
}

.comment-input button {
  margin-left: 10px;
  background-color: #4a6bff;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0 20px;
  cursor: pointer;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  border-bottom: 1px solid #eee;
  padding: 15px 0;
}

.comment-content {
  margin-bottom: 8px;
  font-size: 0.9rem;
}

.comment-meta {
  font-size: 0.8rem;
  color: #777;
}

.delete-btn, .reply-btn {
  cursor: pointer;
  margin-left: 10px;
  color: #ff0000;
}

.delete-btn:hover, .reply-btn:hover {
  text-decoration: underline;
}

.reply-input {
  margin: 10px 0 10px 20px;
  display: flex;
}

.reply-input textarea {
  flex-grow: 1;
  height: 50px;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
}

.reply-input button {
  margin-left: 10px;
  background-color: #4a6bff;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0 15px;
  cursor: pointer;
}

.reply-comment-item {
  margin: 10px 0 10px 20px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
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

.author-badges {
  display: flex;
  gap: 10px;
}

.badge {
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.badge-edit {
  background-color: #4a6bff;
  color: white;
}

.badge-delete {
  background-color: #ff4a4a;
  color: white;
}

@media (max-width: 768px) {
  .board-detail-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .author-badges {
    margin-top: 10px;
    align-self: flex-end;
  }
}
</style>