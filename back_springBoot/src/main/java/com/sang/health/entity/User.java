package com.sang.health.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "이메일 형식이 올바르지 않습니다")  // 이메일 검증 정규 표현식
    private String email;

    @Column(nullable = true) // 소셜 로그인은 비밀번호
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String provider; // "LOCAL", "GOOGLE"
    
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Board> boards;

    // 일반 로그인 사용자 생성 팩토리 메서드
    public static User createLocalUser(String username, String email, String password, String role) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.password = password;
        user.role = role;
        user.provider = "LOCAL";
        return user;
    }

    // 소셜 로그인 사용자 생성 팩토리 메서드
    public static User createSocialUser(String username, String email, String role, String provider) {
        if ("LOCAL".equals(provider)) {
            throw new IllegalArgumentException("소셜 로그인에 LOCAL 제공자를 사용할 수 없습니다");
        }
        
        User user = new User();
        user.username = username;
        user.email = email;
        user.password = null;
        user.role = role;
        user.provider = provider;
        return user;
    }
}