package com.sang.health.service.board;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;

import com.sang.health.entity.Board;
import com.sang.health.entity.BoardES;
import com.sang.health.repository.BoardRepository;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BoardIndexService {
    private final BoardRepository boardRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // 배치 크기 (한 번에 처리할 게시글 수)
    private static final int BATCH_SIZE = 1000;
    private static final String BOARD_INDEX = "board-index";

    public BoardIndexService(BoardRepository boardRepository, ElasticsearchOperations elasticsearchOperations) {
        this.boardRepository = boardRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    // 무중단 재색인
    public void reindexAllBoards() {
        try {
            // 1. 새 인덱스 이름 생성 (타임스탬프 포함)
        	//String newIndexName = "board-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            
            // 새 인덱스 제거 & 생성
            createNewIndex(BOARD_INDEX);
            
            // 데이터 마이그레이션
            migrateData(BOARD_INDEX);
            
            // 4. alias 업데이트 (무중단 전환)
            //updateIndexAlias(BOARD_ALIAS, newIndexName);
            
            log.info("무중단 재색인 완료");
        } catch (Exception e) {
            log.error("재색인 중 오류 발생", e);
            throw new RuntimeException("재색인 실패", e);
        }
    }

    // 새 인덱스 생성
    private void createNewIndex(String indexName) {
        try {
        	IndexOperations indexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
            
        	if (indexOperations.exists()) {
                // 기존 인덱스 삭제
                indexOperations.delete();
                log.info("기존 인덱스 {} 삭제 완료", indexName);
            }
        	
        	Map<String, Object> mappings = Map.of(
                    "properties", Map.of(
                        "id", Map.of(
                            "type", "long",
                            "index", false
                        ),
                        "title", Map.of(
                            "type", "text",
                            "analyzer", "my_custom_analyzer"
                        ),
                        "content", Map.of(
                            "type", "text",
                            "analyzer", "my_custom_analyzer"
                        ),
                        "created", Map.of(
                            "type", "date",
                            "index", false
                        )
                    )
                );

                // 분석기 및 필터 설정
                Map<String, Object> analysis = Map.of(
                    "analyzer", Map.of(
                        "my_custom_analyzer", Map.of(
                            "type", "custom",
                            "char_filter", List.of("my_html_strip"),
                            "tokenizer", "my_nori_tokenizer",
                            "filter", List.of("my_pos_filter", "lowercase_filter", "synonym_filter")
                        )
                    ),
                    "char_filter", Map.of(
                        "my_html_strip", Map.of(
                            "type", "html_strip"
                        )
                    ),
                    "tokenizer", Map.of(
                        "my_nori_tokenizer", Map.of(
                            "type", "nori_tokenizer",
                            "decompound_mode", "mixed",
                            "discard_punctuation", "true",
                            "user_dictionary", "dict/userdict_ko.txt"
                        )
                    ),
                    "filter", Map.of(
                        "my_pos_filter", Map.of(
                            "type", "nori_part_of_speech",
                            "stoptags", List.of("J")
                        ),
                        "lowercase_filter", Map.of(
                            "type", "lowercase"
                        ),
                        "synonym_filter", Map.of(
                            "type", "synonym",
                            "synonyms_path", "dict/synonym-set.txt"
                        )
                    )
                );

                // 기본 설정
                Map<String, Object> settings = Map.of(
                    "number_of_shards", 1,
                    "number_of_replicas", 0,
                    "analysis", analysis
                );

                // 인덱스 생성
                indexOperations.create(settings);
                indexOperations.putMapping(Document.from(mappings));

                log.info("BoardES 인덱스 생성 완료");
        } catch (Exception e) {
            log.error("인덱스 생성 중 오류", e);
            throw new RuntimeException("인덱스 생성 실패", e);
        }
    }

    // 데이터 마이그레이션
    @Transactional(readOnly = true)
    private void migrateData(String newIndexName) {
        log.info("데이터 마이그레이션 시작");

        long totalBoards = boardRepository.count();
        log.info("총 게시글 수: {} 개", totalBoards);

        int totalPages = (int) Math.ceil((double) totalBoards / BATCH_SIZE);

        for (int page = 0; page < totalPages; page++) {
            Pageable pageable = PageRequest.of(page, BATCH_SIZE);
            Page<Board> boardPage = boardRepository.findAll(pageable); // username 까지

            List<BoardES> esBoardList = boardPage.getContent().stream()
                .map(this::convertToESBoard)
                .collect(Collectors.toList());

            // bulkIndex 사용하여 한번에 저장
            List<IndexQuery> queries = esBoardList.stream()
                .map(esBoard -> new IndexQueryBuilder()
                    .withId(String.valueOf(esBoard.getId()))
                    .withObject(esBoard)
                    .build())
                .collect(Collectors.toList());

            elasticsearchOperations.bulkIndex(queries, IndexCoordinates.of(newIndexName));

            log.info("페이지 {}/{} 마이그레이션 완료 - {} 개 게시글", 
                page + 1, totalPages, esBoardList.size());
        }

        log.info("데이터 마이그레이션 완료");
    }

    // Elasticsearch Alias 업데이트
//    private void updateIndexAlias(String aliasName, String newIndexName) {
//        try {
//            IndexOperations indexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(newIndexName));
//            
//            // AliasActions 생성
//            AliasActions aliasActions = new AliasActions();
//            
//            // 기존 alias 제거 (모든 인덱스에서 제거하도록 "*" 사용 – 실제 환경에 맞게 수정 가능)
//            aliasActions.add(new AliasAction.Remove(AliasActionParameters.builder()
//                .withIndices("*")
//                .withAliases(aliasName)
//                .build()));
//                
//            // 새 인덱스에 alias 추가
//            aliasActions.add(new AliasAction.Add(AliasActionParameters.builder()
//                .withIndices(newIndexName)
//                .withAliases(aliasName)
//                .build()));
//            
//            // alias 작업 실행
//            boolean aliasResult = indexOperations.alias(aliasActions);
//            
//            if (aliasResult) {
//                log.info("Alias {} 를 {} 로 업데이트 성공", aliasName, newIndexName);
//            } else {
//                log.error("Alias 업데이트 실패");
//                throw new RuntimeException("Alias 업데이트 실패");
//            }
//        } catch (Exception e) {
//            log.error("Alias 업데이트 중 오류 발생", e);
//            throw new RuntimeException("Alias 업데이트 실패", e);
//        }
//    }

    // Board 엔티티를 BoardES 엔티티로 변환
    private BoardES convertToESBoard(Board board) {
        BoardES esBoard = new BoardES();
        esBoard.setId(board.getId());
        esBoard.setTitle(board.getTitle());
        esBoard.setContent(board.getContent());
        esBoard.setUsername(board.getUser().getUsername()); // User 객체에서 username 가져오기
        esBoard.setCreateDate(new Date(board.getCreateDate().getTime())); // Timestamp to Date 변환
        return esBoard;
    }
}