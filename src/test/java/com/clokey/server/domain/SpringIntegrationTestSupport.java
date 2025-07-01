package com.clokey.server.domain;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional(readOnly = true)
@ActiveProfiles("test")
public abstract class SpringIntegrationTestSupport {
}
