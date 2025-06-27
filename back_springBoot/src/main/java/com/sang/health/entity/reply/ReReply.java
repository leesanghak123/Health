package com.sang.health.entity.reply;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ReReply {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String content;
	
	@Column(nullable = false)
	private String username;
	
	@Column(nullable = false)
	private Long replyid;
	
	@CreationTimestamp
	private Timestamp createDate;
	
	public ReReply(String content, String username, Long replyid) {
	    this.content = content;
	    this.username = username;
	    this.replyid = replyid;
	}
}