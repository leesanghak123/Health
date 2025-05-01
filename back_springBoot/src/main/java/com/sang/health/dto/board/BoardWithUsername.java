package com.sang.health.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardWithUsername {
	private Long id;
    private String title;
    private String content;
    private java.sql.Timestamp createDate;
    private String username;
}
