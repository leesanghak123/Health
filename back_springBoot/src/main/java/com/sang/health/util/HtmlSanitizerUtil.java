package com.sang.health.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class HtmlSanitizerUtil {

    // 화이트 리스트 기반으로 작성 - <script>와 같은 위험한 태그는 허용하지 않고, 안전한 태그들만 허용
    private static final PolicyFactory POLICY = new HtmlPolicyBuilder() // 보안정책

            .allowCommonInlineFormattingElements() // 자주 사용하는 기본 포매팅 엘리먼트
            .allowCommonBlockElements() // 자주 사용하는 기본 엘리먼트

            // 추가 엘리먼트
            .allowElements("hr", "br")
            .allowElements("pre", "code", "img")
            .allowElements("table", "thead", "tbody", "tfoot", "tr", "th", "td")

            .allowAttributes("href", "target").onElements("a") // a 태그에 대해 속성 허용
            .requireRelNofollowOnLinks() // nofollow 추가

            .allowAttributes("src", "alt", "width", "height").onElements("img")

            .allowAttributes("class").onElements("pre", "code", "span")

            .allowAttributes("colspan", "rowspan").onElements("th", "td")

            .toFactory(); // 팩토리화

    public static String sanitize(String html) {
        return POLICY.sanitize(html);
    }

}