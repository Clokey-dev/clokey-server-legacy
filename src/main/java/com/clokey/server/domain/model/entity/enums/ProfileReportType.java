package com.clokey.server.domain.model.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum ProfileReportType {

    FAKE("허위 계정 또는 사칭입니다.", List.of("타인을 사칭하거나 거짓 정보를 이용해 만든 계정", "공식 브랜드, 인플루언서 등을 사칭한 경우", "가짜 계정을 만들어 커뮤니티를 교란하는 경우")),
    SPAM_OR_PROMOTION("스팸 홍보 및 도배 계정입니다", List.of("광고성 메시지를 지속적으로 보내는 계정", "홍보 목적의 프로필 (상업적 링크 다수 포함)","동일한 내용의 글을 반복적으로 게시하는 계정")),
    INAPPROPRIATE("✅ 부적절한 프로필 정보입니다.", List.of("본인 또는 타인의 개인정보(전화번호, 이메일, 주소 등) 포함")),
    ETC("기타", List.of("위 신고 항목에 해당하지 않지만, 부적절하다고 판단되는 프로필"));

    private String title;
    private List<String> contents;

    public static List<Map<String, Object>> getAllReportTypes() {
        return Arrays.stream(values())
                .map(reportType -> Map.of(
                        "name", reportType.name(),
                        "title", reportType.getTitle(),
                        "contents", reportType.getContents()
                ))
                .collect(Collectors.toList());
    }
}
