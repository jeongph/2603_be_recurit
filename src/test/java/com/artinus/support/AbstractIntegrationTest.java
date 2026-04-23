package com.artinus.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest
@ActiveProfiles({"stub", "test"})
public abstract class AbstractIntegrationTest {

    // JVM-wide singleton: 여러 테스트 클래스가 동일한 context 를 공유할 때
    // @Testcontainers 가 afterAll 에서 컨테이너를 stop 시키면
    // 후속 클래스의 캐시된 ServiceConnection 이 죽은 URL 을 참조하게 된다.
    // 따라서 static 초기화 구문으로 한 번만 기동하고 Ryuk 이 JVM 종료 시 정리하도록 맡긴다.
    @ServiceConnection
    @SuppressWarnings("resource")
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("artinus")
            .withUsername("artinus")
            .withPassword("artinus");

    static {
        MYSQL.start();
    }
}
