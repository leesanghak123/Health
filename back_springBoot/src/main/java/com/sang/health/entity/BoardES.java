package com.sang.health.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

@Data
@Document(indexName = "board-index")
public class BoardES {
	
	@Id
    private Long id; // boardId

    @Field(type = FieldType.Text, name = "title")
    private String title;

    @Field(type = FieldType.Text, name = "content")
    private String content;

    @Field(type = FieldType.Keyword, name = "username")
    private String username;

    @Field(type = FieldType.Date, name = "createDate")
    private Date createDate;
}