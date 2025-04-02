package com.sang.health.service.user;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.sang.health.dto.user.CustomOAuth2User;
import com.sang.health.dto.user.GoogleResponse;
import com.sang.health.dto.user.NaverResponse;
import com.sang.health.dto.user.OAuth2Response;
import com.sang.health.dto.user.UserDTO;
import com.sang.health.entity.user.User;
import com.sang.health.repository.user.UserRepository;

import jakarta.transaction.Transactional;

// DefaultOAuth2UserService를 상속 받아서 구현
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService{
	
	private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }
	
	@Override
	@Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		// 클래스가 가지는 생성자를 super를 통해 불러서 값을 획득
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 출처 확인(google, naver)을 해야한다 -> 서로 응답 값(방식)이 다르기 때문에 Dto를 따로 작성
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        OAuth2Response oAuth2Response = null;
        
        // naver라면
        if (registrationId.equals("naver")) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        // google이라면
        else if (registrationId.equals("google")) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        // 아무것도 아니라면
        else {

            return null;
        }
        
        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값 만들기
        String username = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        String email = oAuth2Response.getEmail();
        String provider = oAuth2Response.getProvider().toUpperCase();
        
        User existData = userRepository.findByUsername(username).orElse(null);

        // 소셜ID가 존재하지 않는 경우 - 새 사용자 생성
        if (existData == null) {
            // 팩토리 메서드를 사용하여 소셜 로그인 사용자 생성
            User newUser = User.createSocialUser(
                username,
                email,
                "ROLE_USER",
                provider
            );
            
            userRepository.save(newUser);
            
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            userDTO.setRole("ROLE_USER");
            return new CustomOAuth2User(userDTO);
        }
        // 소셜ID가 존재하는 경우
        else {
        	// email 업데이트
            existData.setEmail(oAuth2Response.getEmail());

            userRepository.save(existData);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(existData.getUsername());
            userDTO.setRole(existData.getRole());

            return new CustomOAuth2User(userDTO);
        }
    }
}
