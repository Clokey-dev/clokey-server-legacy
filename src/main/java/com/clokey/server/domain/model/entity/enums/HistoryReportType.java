package com.clokey.server.domain.model.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum HistoryReportType {

    SEXUAL("음란물, 또는 선정적인 내용입니다.", List.of("성적인 묘사, 이미지·영상 포함", "노출이 과도한 사진 또는 부적절한 설명 포함")),
    VIOLENT("폭력적이거나 불법적인 내용을 포함하고 있습니다", List.of("폭력, 학대, 자해, 살해 협박 등 위험한 행동 조장", "불법 행위를 암시하거나 조장하는 게시물 (예: 불법 약물, 도박 등)")),
    HARMFUL_TO_MINORS("청소년에게 유해한 내용입니다.", List.of("청소년이 보기에 부적절한 주제 (예: 성인용품, 음주, 흡연 관련 내용)", "자극적인 장면, 범죄 미화 등 청소년 보호법 위반 가능성이 있는 콘텐츠")),
    PRIVACY_EXPOSURE("개인정보 노출 게시물입니다.", List.of("본인 또는 타인의 연락처, 주소, 신분증 등 개인정보가 포함된 게시물", "사적인 대화 내용이 공개된 경우")),
    ANNOYING("악의적이거나 불쾌감을 유발하는 표현입니다.", List.of("특정 개인이나 집단을 비하하는 내용 (성별, 인종, 종교 차별 등)", "심한 욕설, 모욕적인 언어, 혐오 표현 포함")),
    ETC("기타", List.of("위 신고 항목에 해당하지 않지만, 부적절하다고 판단되는 게시물"));

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
