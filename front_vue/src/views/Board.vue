<template>
  <div class="board-container">
    <h2 class="board-title">자유게시판</h2>

    <div class="write-button-container">
      <button @click="goToWritePage" class="btn btn-write">글쓰기</button>
    </div>

    <div class="table-responsive">
      <table class="board-table">
        <thead>
          <tr>
            <th class="col-num">순번</th>
            <th class="col-title">제목</th>
            <th class="col-author">글쓴이</th>
            <th class="col-date">작성일</th>
            <th class="col-view">조회수</th>
            <th class="col-like">추천수</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="board in boards" :key="board.id" @click="goToDetailPage(board.id)" class="clickable-row">
            <td class="col-num">{{ board.id }}</td>
            <td class="col-title text-ellipsis">{{ board.title }}</td>
            <td class="col-author text-ellipsis">{{ board.username }}</td>
            <td class="col-date">{{ formatDate(board.createDate) }}</td>
            <td class="col-view">{{ board.count }}</td>
            <td class="col-like">{{ board.likeCnt }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <ul class="pagination">
      <li class="page-item" :class="{ disabled: currentPage === 1 }">
        <a class="page-link" @click.prevent="changePage(currentPage - 1)">&laquo;</a>
      </li>
      <li v-for="i in totalPages" :key="i" class="page-item" :class="{ active: currentPage === i }">
        <a class="page-link" @click.prevent="changePage(i)">{{ i }}</a>
      </li>
      <li class="page-item" :class="{ disabled: currentPage === totalPages }">
        <a class="page-link" @click.prevent="changePage(currentPage + 1)">&raquo;</a>
      </li>
    </ul>
  </div>
</template>

<script>
import apiClient from '@/services/reissue';

export default {
  data() {
    return {
      boards: [],
      currentPage: 1,
      totalPages: 0,
    };
  },
  methods: {
    async fetchBoards(page = 1) {
      const response = await apiClient.get(`/board?page=${page - 1}`);
      this.boards = response.data.content;
      this.totalPages = response.data.totalPages;
      this.currentPage = page;
    },

    changePage(page) {
      if (page > 0 && page <= this.totalPages) {
        this.fetchBoards(page);
      }
    },

    formatDate(dateString) {
      const options = { year: 'numeric', month: '2-digit', day: '2-digit' };
      return new Date(dateString).toLocaleDateString('ko-KR', options);
    },

    goToWritePage() {
      this.$router.push('/board/write');
    },

    goToDetailPage(boardId) {
      this.$router.push(`/board/detail/${boardId}`);
    },
  },
  mounted() {
    this.fetchBoards(this.currentPage);
  },
};
</script>

<style scoped>
/* 게시판 컨테이너 스타일 - 화면 전체에 맞춤 */
.board-container {
  width: 100%;
  height: 100vh;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 0;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 게시판 제목 스타일 */
.board-title {
  text-align: center;
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
  flex-shrink: 0;
}

/* 테이블 컨테이너 */
.table-responsive {
  width: 100%;
  overflow-x: auto;
  flex-grow: 1;
}

/* 게시판 테이블 스타일 */
.board-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.board-table th,
.board-table td {
  padding: 12px 15px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
}

.board-table th {
  background-color: #6c757d;
  color: white;
  font-weight: bold;
  border: none;
}

.board-table td {
  background-color: #fff;
  color: #555;
}

/* 컬럼 너비 비율 설정 */
.col-num {
  width: 8%;
}

.col-title {
  width: 45%;
  text-align: left;
}

.col-author {
  width: 15%;
}

.col-date {
  width: 15%;
}

.col-view, .col-like {
  width: 8%;
}

/* 텍스트 오버플로우 처리 */
.text-ellipsis {
  text-overflow: ellipsis;
  overflow: hidden;
}

.board-table tr:nth-child(even) td {
  background-color: #f3f3f3;
}

.board-table tr:hover td {
  background-color: #e9ecef;
}

/* 클릭 가능한 테이블 행 스타일 */
.clickable-row {
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.clickable-row:hover {
  background-color: #f1f1f1;
}

/* 페이지네이션 스타일 */
.pagination {
  display: flex;
  justify-content: center;
  padding-top: 20px;
  gap: 5px;
  flex-shrink: 0;
  list-style: none;
  margin: 0;
  padding-left: 0;
}

.page-item {
  list-style: none;
}

.page-link {
  display: block;
  padding: 8px 12px;
  margin: 0 3px;
  color: #6c757d;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
}

.page-link:hover {
  background-color: #6c757d;
  color: #fff;
}

.page-item.disabled .page-link {
  color: #ccc;
  cursor: not-allowed;
  pointer-events: none;
}

.page-item.active .page-link {
  background-color: #6c757d;
  color: white;
  border-color: #6c757d;
}

/* 글쓰기 버튼 스타일 */
.write-button-container {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.btn-write {
  background-color: #f3f3f3;
  color: #555;
  border: 1px solid #ddd;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  font-size: 16px;
}

.btn-write:hover {
  background-color: #e9ecef;
}

.btn-write:focus {
  outline: none;
}

/* 반응형 스타일 */
@media (max-width: 768px) {
  .col-date {
    display: none;
  }
  
  .col-num {
    width: 10%;
  }
  
  .col-title {
    width: 50%;
  }
  
  .col-author {
    width: 20%;
  }
  
  .col-view, .col-like {
    width: 10%;
  }
}

@media (max-width: 576px) {
  .col-author {
    display: none;
  }
  
  .col-num {
    width: 15%;
  }
  
  .col-title {
    width: 55%;
  }
  
  .col-view, .col-like {
    width: 15%;
  }
}
</style>