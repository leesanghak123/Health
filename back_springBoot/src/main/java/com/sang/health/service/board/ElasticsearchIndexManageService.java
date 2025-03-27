//package com.sang.health.service.board;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
//import org.springframework.data.elasticsearch.core.IndexOperations;
//import org.springframework.data.elasticsearch.core.index.AliasActions;
//import org.springframework.data.elasticsearch.core.index.AliasAction;
//import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
//import org.springframework.stereotype.Service;
//
//import com.sang.health.entity.Board;
//import com.sang.health.entity.BoardES;
//import com.sang.health.repository.BoardRepository;
//
//@Service
//public class ElasticsearchIndexManageService {
//    private final ElasticsearchOperations elasticsearchOperations;
//    private final BoardRepository boardRepository;
//
//    public ElasticsearchIndexManageService(
//        ElasticsearchOperations elasticsearchOperations, 
//        BoardRepository boardRepository
//    ) {
//        this.elasticsearchOperations = elasticsearchOperations;
//        this.boardRepository = boardRepository;
//    }
//
//    private static final String ALIAS_NAME = "board_index";
//    private static final String INDEX_PREFIX = "board_index_";
//
//    public void createNewIndexAndReindex() {
//        // 새 인덱스 이름 생성 (타임스탬프 기반)
//        String newIndexName = INDEX_PREFIX + System.currentTimeMillis();
//
//        // 1. 새 인덱스 생성
//        IndexOperations indexOperations = elasticsearchOperations.indexOps(BoardES.class);
//
//        // 인덱스 설정
//        Map<String, Object> settings = Map.of(
//            "index.number_of_shards", 1,
//            "index.number_of_replicas", 0
//        );
//
//        // 새 인덱스 생성 및 매핑 설정
//        indexOperations.create(settings);
//        indexOperations.putMapping();
//
//        // 2. 데이터베이스에서 데이터 조회 및 새 인덱스에 색인
//        List<Board> boards = boardRepository.findAll();
//        List<BoardES> boardESList = boards.stream()
//            .map(board -> {
//                BoardES boardES = new BoardES();
//                boardES.setId(board.getId().toString());
//                boardES.setTitle(board.getTitle());
//                boardES.setContent(board.getContent());
//                boardES.setUsername(board.getUser().getUsername());
//                boardES.setCreateDate(board.getCreateDate());
//                return boardES;
//            })
//            .collect(Collectors.toList());
//
//        // 3. 대량 색인
//        IndexCoordinates indexCoordinates = IndexCoordinates.of(newIndexName);
//        elasticsearchOperations.save(boardESList, indexCoordinates);
//
//        // 4. Alias 관리
//        updateAlias(newIndexName);
//    }
//
//    private void updateAlias(String newIndexName) {
//        // AliasActions 객체 생성
//        AliasActions aliasActions = new AliasActions();
//
//        // 기존 인덱스 조회
//        List<String> existingIndices = elasticsearchOperations.indexOps(BoardES.class)
//            .getAliases()
//            .keySet()
//            .stream()
//            .filter(index -> index.startsWith(INDEX_PREFIX))
//            .collect(Collectors.toList());
//
//        // 기존 인덱스에서 alias 제거
//        for (String existingIndex : existingIndices) {
//            aliasActions.add(AliasAction.remove().alias(ALIAS_NAME).index(existingIndex));
//        }
//
//        // 새 인덱스에 alias 추가
//        aliasActions.add(AliasAction.add().alias(ALIAS_NAME).index(newIndexName));
//
//        // alias 업데이트
//        elasticsearchOperations.indexOps(BoardES.class).updateAlias(aliasActions);
//    }
//}