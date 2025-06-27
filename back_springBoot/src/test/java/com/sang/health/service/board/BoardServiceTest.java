package com.sang.health.service.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sang.health.dto.board.BoardFindDto;
import com.sang.health.entity.board.Board;
import com.sang.health.entity.user.User;
import com.sang.health.repository.board.BoardCountRepository;
import com.sang.health.repository.board.BoardESRepository;
import com.sang.health.repository.board.BoardLikeRepository;
import com.sang.health.repository.board.BoardLikeTotalRepository;
import com.sang.health.repository.board.BoardRepository;
import com.sang.health.repository.reply.ReReplyRepositoy;
import com.sang.health.repository.reply.ReplyRepository;
import com.sang.health.repository.user.UserRepository;
import com.sang.health.util.RedisUtil;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

	@InjectMocks
	BoardService boardService;
	
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private BoardCountRepository boardCountRepository;
    @Mock
    private BoardLikeTotalRepository boardLikeTotalRepository;
    @Mock
    private BoardLikeRepository boardLikeRepository;
    @Mock
    private BoardESRepository boardESRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private ReReplyRepositoy reReplyRepositoy;
    
    private User user1;
    private User user2;
    private Board board1;
    private Board board2;
    private Pageable pageable;
    
	@Test
	@DisplayName("글 목록 조회 시, 반환 및 내용 검증")
	void test글목록보기() {
		
		// given
		user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        board1 = new Board();
        board1.setId(1L);
        board1.setTitle("제목1");
        board1.setContent("내용1");
        board1.setUser(user1);
        board1.setCreateDate(Timestamp.from(Instant.now()));

        board2 = new Board();
        board2.setId(2L);
        board2.setTitle("제목2");
        board2.setContent("내용2");
        board2.setUser(user2);
        board2.setCreateDate(Timestamp.from(Instant.now().plusSeconds(3600))); // now()+1시간

        pageable = PageRequest.of(0, 10); // 첫 번째 페이지, 10개 항목 (Controller 단의 설정)

        List<Board> boardList = Arrays.asList(board1, board2);
        Page<Board> mockBoardPage = new PageImpl<>(boardList, pageable, boardList.size()); // Page 구현
        when(boardRepository.findAll(pageable)).thenReturn(mockBoardPage);

        when(redisUtil.getBoardView(1L)).thenReturn(10); // board1의 조회수
        when(redisUtil.getBoardView(2L)).thenReturn(20); // board2의 조회수

        when(redisUtil.getBoardLikeTotal(1L)).thenReturn(5); // board1의 추천수
        when(redisUtil.getBoardLikeTotal(2L)).thenReturn(8); // board2의 추천수
		
        
		// when
		Page<BoardFindDto> resultPage = boardService.글목록(pageable);
		
		// then
		verify(boardRepository, times(1)).findAll(pageable); // boardRepository.findAll(pageable)이 정확히 한 번 호출되었는지 검증
		
		verify(redisUtil, times(1)).getBoardView(1L); // redisUtil 메소드들이 각 게시글 ID에 대해 올바르게 호출되었는지 검증
        verify(redisUtil, times(1)).getBoardView(2L);
        verify(redisUtil, times(1)).getBoardLikeTotal(1L);
        verify(redisUtil, times(1)).getBoardLikeTotal(2L);
        
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(2); // 총 요소 수
        assertThat(resultPage.getTotalPages()).isEqualTo(1);    // 총 페이지 수 (10개씩 가져오므로)
        assertThat(resultPage.getContent()).hasSize(2);         // 현재 페이지의 요소 수
        
        
        List<BoardFindDto> dtoList = resultPage.getContent();

        BoardFindDto dto1 = dtoList.get(0);
        assertThat(dto1.getId()).isEqualTo(board1.getId());
        assertThat(dto1.getTitle()).isEqualTo(board1.getTitle());
        assertThat(dto1.getUsername()).isEqualTo(board1.getUser().getUsername());
        assertThat(dto1.getCreateDate()).isEqualTo(board1.getCreateDate());
        assertThat(dto1.getCount()).isEqualTo(10);
        assertThat(dto1.getLikeCnt()).isEqualTo(5);

        BoardFindDto dto2 = dtoList.get(1);
        assertThat(dto2.getId()).isEqualTo(board2.getId());
        assertThat(dto2.getTitle()).isEqualTo(board2.getTitle());
        assertThat(dto2.getUsername()).isEqualTo(board2.getUser().getUsername());
        assertThat(dto2.getCreateDate()).isEqualTo(board2.getCreateDate());
        assertThat(dto2.getCount()).isEqualTo(20);
        assertThat(dto2.getLikeCnt()).isEqualTo(8);
	}

}