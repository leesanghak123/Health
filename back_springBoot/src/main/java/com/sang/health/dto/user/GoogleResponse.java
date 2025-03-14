package com.sang.health.dto.user;

import java.util.Map;

public class GoogleResponse implements OAuth2Response{

    private final Map<String, Object> attribute;

    public GoogleResponse(Map<String, Object> attribute) {
    	// resultcode=00, message=success, id=123123123, name=이상학
    	// google의 경우 key 내부에 data가 없기 때문에 response를 get를 하지 않는다
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {

        return "google";
    }

    @Override
    public String getProviderId() {

        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {

        return attribute.get("email").toString();
    }
}