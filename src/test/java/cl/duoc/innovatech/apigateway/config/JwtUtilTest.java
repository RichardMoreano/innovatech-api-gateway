package cl.duoc.innovatech.apigateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret = "01234567890123456789012345678901"; // 32 bytes

    @BeforeEach
    void inicializar() {
        // Inicializamos la clase bajo prueba y le inyectamos la "secret" para firmar/validar JWT
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
    }

    @Test
    void testGenerarYValidarTokenExitoso() {
        // Genero un token firmado con la misma clave y luego lo valido con JwtUtil
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .setSubject("user1")
                .claim("role", "ADMIN")
                .setIssuedAt(new Date())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        Claims claims = jwtUtil.validateAndGetClaims(token);

        // Compruebo que el subject y el claim role lleguen correctamente
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("user1");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void testValidarTokenInvalido() {
        // Simulo un token mal formado y espero que la validación lance una excepción
        String badToken = "this.is.not.a.token";
        assertThrows(Exception.class, () -> jwtUtil.validateAndGetClaims(badToken));
    }
}
