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
            autocomplete="off"
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
import apiClient from '@/services/reissue';

export default {
name: "BoardWrite",
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
},
methods: {
    initSummernote() {
    // Summernote 초기화
    if (window.jQuery) {
        $("#summernote").summernote({
        placeholder: "여기에 글을 작성해주세요.",
        tabsize: 2,
        height: 300,
        lang: 'ko-KR',
        toolbar: [
            ['style', ['style']],
            ['font', ['bold', 'italic', 'underline', 'clear']],
            ['color', ['color']],
            ['para', ['ul', 'ol', 'paragraph']],
            ['table', ['table']],
            ['insert', ['link', 'picture']],
            ['view', ['fullscreen', 'codeview']]
        ],
        callbacks: {
            onInit: function() {
            console.log('Summernote가 성공적으로 초기화되었습니다.');
            }
        }
        });
    } else {
        console.error('jQuery가 로드되지 않았습니다.');
    }
    },
    
    async submitForm() {
    try {
        // 제목이 비어있는지 확인
        if (!this.board.title.trim()) {
        alert("제목을 입력해주세요.");
        return;
        }

        // Summernote 에디터 내용 HTML로 변환
        if (window.jQuery) {
        this.board.content = $("#summernote").summernote("code");
        
        // 내용이 비어있는지 확인 (HTML 태그를 제외한 텍스트)
        const contentText = $(this.board.content).text().trim();
        if (!contentText && !$(this.board.content).find('img').length) {
            alert("내용을 입력해주세요.");
            return;
        }
        }

        // JWT 토큰 가져오기
        const token = localStorage.getItem("access");

        // 게시글 작성 요청
        await apiClient.post(
        "/board/write",
        this.board,
        {
            headers: {
            access: token,
            },
        }
        );

        alert("글이 성공적으로 작성되었습니다!");
        this.$router.push("/");
    } catch (error) {
        // 에러 처리
        if (error.response && error.response.status === 401) {
        alert("인증이 만료되었습니다. 다시 로그인해주세요.");
        localStorage.removeItem("access");
        this.$router.push("/login");
        } else {
        console.error("글 작성 실패:", error);
        alert("글 작성에 실패했습니다. 다시 시도해주세요.");
        }
    }
    },
},
beforeUnmount() {
    // 컴포넌트 제거 시 Summernote 정리
    if (window.jQuery) {
    $("#summernote").summernote('destroy');
    }
}
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
border: none;
}

.btn-beige:hover {
background-color: #eae0c8;
color: #000;
}
</style>