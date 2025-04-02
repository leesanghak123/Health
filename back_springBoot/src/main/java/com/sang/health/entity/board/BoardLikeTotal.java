package com.sang.health.entity.board;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class BoardLikeTotal {

	@Id
	private Long boardId;
	
	@Column(nullable = false)
	private int likeTotal;
		
}