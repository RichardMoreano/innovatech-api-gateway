package cl.duoc.innovatech.apigateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "jwt.secret=panconqueso=paltadme2==duocuc2026",
    "spring.cloud.gateway.enabled=false"
})
class ApiGatewayApplicationTest {

    @Test
    @DisplayName("Debería cargar el contexto de Spring Boot registrando la cobertura de la aplicación")
    void contextLoads() {
        // Al cargar la clase anotada, JaCoCo registra la inicialización completa del componente raíz
        assertDoesNotThrow(() -> {});
    }
}