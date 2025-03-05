package com.clokey.server.domain.model.entity.enums;

public enum HistoryReport {
    PORNOGRAPHIC_CONTENT,    // 성적인 묘사, 음란한 이미지 등
    VIOLENT_OR_ILLEGAL_CONTENT, // 폭력, 학대, 자해, 살해 협박, 불법 행위
    HARMFUL_TO_MINORS,          // 성인용품, 음주, 흡연 등 청소년 보호법 위반 가능성
    PERSONAL_INFORMATION_EXPOSURE,  // 연락처, 주소, 신분증 등 개인정보 포함
    MALICIOUS_OR_OFFENSIVE_EXPRESSION, // 비하, 혐오 표현, 욕설
    OTHER // 기타 (직접 입력 가능)
}
