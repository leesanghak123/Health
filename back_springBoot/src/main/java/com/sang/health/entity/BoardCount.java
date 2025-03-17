package com.sang.health.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class BoardCount {

	@Id
	private Long boardId;
	
	@Column(nullable = false)
	private int count;
}
