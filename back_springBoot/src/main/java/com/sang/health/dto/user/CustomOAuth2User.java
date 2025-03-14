package com.sang.health.dto.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User implements OAuth2User {

    private final UserDTO userDTO;

    public CustomOAuth2User(UserDTO userDTO) {

        this.userDTO = userDTO;
    }

    // 받은 데이터 값을 return 해주는 부분
    // Google, Naver의 값을 획일화 하기 힘들어서 사용 X
    @Override
    public Map<String, Object> getAttributes() {

        return null;
    }

    // Role 값 return
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

                return userDTO.getRole();
            }
        });

        return collection;
    }

	@Override
	public String getName() {
		
		return userDTO.getUsername();
	}
}