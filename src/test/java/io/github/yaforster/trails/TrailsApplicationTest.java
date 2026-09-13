package io.github.yaforster.trails;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=create-drop", })
public class TrailsApplicationTest {

	@Test
	void contextLoads() {

	}

}
