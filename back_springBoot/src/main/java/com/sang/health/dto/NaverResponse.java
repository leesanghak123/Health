package com.sang.health.dto;

import java.util.Map;

public class NaverResponse implements OAuth2Response{

	// 응답은 JSON, key-value의 형태
	// DI 개념은 아니고 불변을 보장한다는 뜻
    private final Map<String, Object> attribute;

    public NaverResponse(Map<String, Object> attribute) {

    	// resultcode=00, message=success, response={id=123123123, name=이상학}
    	// naver의 경우 이렇게 2중 key 형식으로 담기기 때문에 response를 get
        this.attribute = (Map<String, Object>) attribute.get("response");
    }

    @Override
    public String getProvider() {

        return "naver";
    }

    @Override
    public String getProviderId() {

        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {

        return attribute.get("email").toString();
    }

}
