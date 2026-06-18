package cl.duoc.innovatech.apigateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secretoPrueba = "panconqueso=paltadme2==duocuc2026"; 

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Cargamos la clave secreta en la instancia del componente
        ReflectionTestUtils.setField(jwtUtil, "secret", secretoPrueba);
    }

    @Test
    @DisplayName("Debería leer los claims si el token es válido")
    void testValidarYObtenerClaimsTokenValido() {
        SecretKey key = Keys.hmacShaKeyFor(secretoPrueba.getBytes(StandardCharsets.UTF_8));
        
        String token = Jwts.builder()
                .subject("usuarioPrueba")
                .claim("roles", "ROLE_ADMIN")
                .claim("userId", 10L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        Claims claims = jwtUtil.validateAndGetClaims(token);

        assertNotNull(claims);
        assertEquals("usuarioPrueba", claims.getSubject());
        assertEquals("ROLE_ADMIN", claims.get("roles", String.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción si el token está dañado o es inválido")
    void testValidarYObtenerClaimsTokenInvalido() {
        String tokenMalo = "un.token.completamente.roto";
        assertThrows(RuntimeException.class, () -> jwtUtil.validateAndGetClaims(tokenMalo));
    }
}