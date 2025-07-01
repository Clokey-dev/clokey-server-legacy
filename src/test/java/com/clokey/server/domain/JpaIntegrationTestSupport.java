package com.clokey.server.domain;

import com.clokey.server.global.config.QuerydslConfig;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Disabled
@DataJpaTest
@ActiveProfiles("test")
@Import(QuerydslConfig.class)
public abstract class JpaIntegrationTestSupport {
}
