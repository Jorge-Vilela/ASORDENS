package br.edu.nassau.asordens;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:context;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "api.security.token.secret=chave-exclusiva-para-testes-automatizados"
})
class AsOrdensApplicationTests {

	@Test
	void contextLoads() {
	}

}
