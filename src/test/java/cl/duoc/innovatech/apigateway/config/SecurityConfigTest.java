package cl.duoc.innovatech.apigateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "jwt.secret=panconqueso=paltadme2==duocuc2026",
    "spring.cloud.gateway.enabled=false"
})
class SecurityConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Debería validar que el Bean de la cadena de filtros de seguridad exista en el contexto")
    void testSecurityWebFilterChainBeanExists() {
        SecurityWebFilterChain chain = context.getBean(SecurityWebFilterChain.class);
        assertNotNull(chain, "La cadena de filtros de seguridad debe estar registrada en el contexto");
    }

    @Test
    @DisplayName("Debería validar que el Bean del filtro CORS exista en el contexto")
    void testCorsWebFilterBeanExists() {
        CorsWebFilter filter = context.getBean(CorsWebFilter.class);
        assertNotNull(filter, "El filtro CORS debe estar registrado en el contexto");
    }
}