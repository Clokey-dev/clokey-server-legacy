package com.clokey.server.domain;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(controllers = {
        // 여기에 추가하세요
})
public abstract class WebMvcIntegrationTestSupport {
}
