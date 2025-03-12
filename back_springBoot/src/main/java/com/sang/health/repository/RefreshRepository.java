//package com.sang.health.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import com.sang.health.entity.Refresh;
//
//import jakarta.transaction.Transactional;
//
//public interface RefreshRepository extends JpaRepository<Refresh, Long>{
//
//	// 존재 확인
//	Boolean existsByRefresh(String refresh);
//
//	// 삭제
//    void deleteByRefresh(String refresh);
//}
