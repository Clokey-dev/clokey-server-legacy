package com.clokey.server.domain.history.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class HashtagSerializer extends JsonSerializer<List<String>> {
    @Override
    public void serialize(List<String> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // 모든 해시태그 앞에 '#'이 붙어있는지 확인 후 유지
        List<String> processedHashtags = value.stream()
                .map(tag -> tag.startsWith("#") ? tag : "#" + tag) // #이 없으면 추가
                .collect(Collectors.toList());

        gen.writeObject(processedHashtags);
    }
}

