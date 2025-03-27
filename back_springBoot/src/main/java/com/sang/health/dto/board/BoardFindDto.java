package com.sang.health.dto.board;

import java.time.Instant;
import java.sql.Timestamp;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BoardFindDto {
    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String username;

    private Timestamp createDate;
    private int count;
    private int likeCnt;

    // Instant를 받는 생성자 추가
    public BoardFindDto(Long id, String title, String username, Instant createDate, int count, int likeCnt) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.createDate = createDate != null ? Timestamp.from(createDate) : null;
        this.count = count;
        this.likeCnt = likeCnt;
    }
}