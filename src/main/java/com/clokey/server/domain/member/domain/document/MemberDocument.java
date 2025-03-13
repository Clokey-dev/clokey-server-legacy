package com.clokey.server.domain.member.domain.document;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "user")
public class MemberDocument {

    @Id
    private Long id;  // JPA 엔티티와 동일한 id 사용

    private String nickname;

    private String clokeyId;

    private String profileUrl;
}
