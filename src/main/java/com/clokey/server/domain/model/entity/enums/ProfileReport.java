package com.clokey.server.domain.model.entity.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum ProfileReport {
    FAKE,
    SPAM,
    INAPPROPRIATE

    SWEARING_AND_CURSING("욕설 및 비방이 포함되어 있습니다.", List.of("심한 욕설, 인격 모독, 명예훼손 발언 포함", "상대를 조롱하거나 비하하는 내용")),
    DISCRIMINATORY_AND_HATEFUL("혐오 및 차별적 표현입니다", List.of("성별, 인종, 장애, 종교 등을 이유로 한 차별적 발언", "특정 그룹을 혐오하거나 배척하는 표현")),
    SPAM_OR_PROMOTION("스팸 홍보 및 도배 댓글입니다", List.of("반복적인 동일 댓글(광고 포함) 작성", "특정 링크를 다수 포함한 홍보성 댓글")),
    PRIVATE_INFO("사적인 정보가 포함된 댓글입니다.", List.of("본인 또는 타인의 개인정보(전화번호, 이메일, 주소 등) 포함")),
    ANNOYING("불쾌감을 주는 표현입니다.", List.of("조롱, 악의적인 비꼼, 공격적인 표현 포함")),
    ETC("기타", List.of("위 신고 항목에 해당하지 않지만, 부적절하다고 판단되는 댓글"));

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
