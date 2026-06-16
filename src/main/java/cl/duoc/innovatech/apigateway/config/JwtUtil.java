package cl.duoc.innovatech.apigateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtUtil {

    @Value("${security.jwt.secret:secret-key-please-change}")
    private String secret;

    // Validamos y extraemos los claims del token JWT usando la clave secreta.
    // Nota para desarrollo: la clave viene de application.properties y es sólo un ejemplo.
    // En producción deberíamos traer la clave desde un vault y rotarla periódicamente.
    public Claims validateAndGetClaims(String token) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
