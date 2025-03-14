<template>
<div class="container mt-5">
    <div class="card shadow-sm">
    <div class="card-body">
        <form @submit.prevent="submitForm">
        <div class="form-group mb-4">
            <label for="title" class="form-label fw-bold">제목</label>
            <input
            id="title"
            type="text"
            class="form-control"
            placeholder="Enter title"
            v-model="board.title"
            required
            />
        </div>

        <div id="summernote"></div>

        <div class="text-end mt-4">
            <button class="btn btn-beige btn-sm px-3" type="submit">저장</button>
        </div>
        </form>
    </div>
    </div>
</div>
</template>

<script>
import apiClient from "@/services/reissue";
import $ from 'jquery';

export default {
name: "BoardUpdate",
data() {
    return {
    board: {
        title: "",
        content: "",
    },
    };
},
mounted() {
    // Summernote 초기화를 1ms 지연시킴
    setTimeout(() => {
    this.initSummernote();
    }, 1);
    this.fetchBoard();
},
methods: {
    initSummernote() {
    // Summernote 초기화 - 없다면 추가
    if ($("#summernote").summernote) {
        $("#summernote").summernote({
        height: 300,
        minHeight: 200,
        maxHeight: 500,
        focus: false,
        lang: 'ko-KR',
        placeholder: '내용을 입력해주세요',
        toolbar: [
            ['style', ['style']],
            ['font', ['bold', 'italic', 'underline', 'clear']],
            ['color', ['color']],
            ['para', ['ul', 'ol', 'paragraph']],
            ['table', ['table']],
            ['insert', ['link', 'picture']],
            ['view', ['fullscreen', 'codeview', 'help']]
        ]
        });
    }
    },
    
    async fetchBoard() {
    try {
        const boardId = this.$route.params.id;
        const token = localStorage.getItem("access"); // jwt → access로 수정

        const response = await apiClient.get(`/board/${boardId}`, {
        headers: {
            access: token,
        },
        });

        this.board.title = response.data.title;
        this.board.content = response.data.content;

        // Summernote 에디터에 기존 게시글 내용 설정
        if ($("#summernote").summernote) {
        $("#summernote").summernote("code", this.board.content);
        }
        
    } catch (error) {
        console.error("게시글 불러오기 실패:", error);
        alert("게시글을 불러오는데 실패했습니다.");
    }
    },
    
    async submitForm() {
    try {
        // Summernote 에디터 내용 HTML로 변환
        if ($("#summernote").summernote) {
        this.board.content = $("#summernote").summernote("code");
        }

        const token = localStorage.getItem("access");
        const boardId = this.$route.params.id;
        
        await apiClient.patch(
        `/board/update/${boardId}`,
        this.board,
        {
            headers: {
            access: token,
            },
        }
        );

        alert("글이 성공적으로 수정되었습니다!");
        this.$router.push(`/board/detail/${boardId}`);
    } catch (error) {
        console.error("글 수정 실패:", error);
        alert("글 수정에 실패했습니다. 다시 시도해주세요.");
    }
    },
},
};
</script>

<style scoped>
.container {
max-width: 800px;
}

.card {
border-radius: 10px;
}

.form-control:focus {
border-color: #007bff;
box-shadow: 0 0 5px rgba(0, 123, 255, 0.5);
}

.btn-beige {
background-color: #f5f5dc;
color: #333;
}

.btn-beige:hover {
background-color: #eae0c8;
color: #000;
}
</style>