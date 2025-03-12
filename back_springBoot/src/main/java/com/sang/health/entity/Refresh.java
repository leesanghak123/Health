//package com.sang.health.entity;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import lombok.Data;
//
//@Entity
//@Data
//public class Refresh {
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//	// 여러 디바이스에서 로그인 하는 경우
//    private String username;
//    
//    @Column(unique = true)
//    private String refresh;
//    
//    private String expiration; // 만료 시간
//}