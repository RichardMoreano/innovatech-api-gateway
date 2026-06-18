package cl.duoc.innovatech.apigateway.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    private JwtUtil jwtUtil;
    private JwtFilter jwtFilter;

    @Mock
    private WebFilterChain filterChain;

    private MockServerWebExchange exchange;
    private final String secretoPrueba = "panconqueso=paltadme2==duocuc2026";

    @BeforeEach
    void setUp() {
        // Inicializamos el JwtUtil real y le pasamos el secreto de prueba
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secretoPrueba);

        // Inicializamos el filtro con la instancia real de JwtUtil
        jwtFilter = new JwtFilter();
        ReflectionTestUtils.setField(jwtFilter, "jwtUtil", jwtUtil);

        // Petición base vacía para los tests sencillos
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v2/bff/proyectos").build();
        exchange = MockServerWebExchange.from(request);
    }

    @Test
    @DisplayName("Prueba 1: Si no viene el header Authorization, pasa de largo al siguiente filtro")
    void testFiltrarSinHeaderAuthorization() {
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> resultado = jwtFilter.filter(exchange, filterChain);

        StepVerifier.create(resultado).verifyComplete();
        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    @DisplayName("Prueba 2: Si el token es válido, muta la petición agregando los headers del usuario")
    void testFiltrarTokenValidoAgregaHeaders() {
        SecretKey key = Keys.hmacShaKeyFor(secretoPrueba.getBytes(StandardCharsets.UTF_8));
        
        // Sincronizado con JwtFilter
        String tokenReal = Jwts.builder()
                .subject("richard")
                .claim("role", "ROLE_ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        MockServerHttpRequest requestConToken = MockServerHttpRequest.get("/api/v2/bff/proyectos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenReal)
                .build();
        MockServerWebExchange exchangeConToken = MockServerWebExchange.from(requestConToken);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        Mono<Void> resultado = jwtFilter.filter(exchangeConToken, filterChain);

        StepVerifier.create(resultado).verifyComplete();

        // Usamos ServerWebExchange para interceptar cualquier implementación reactiva interna de Spring
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(filterChain).filter(captor.capture());

        HttpHeaders headersResultantes = captor.getValue().getRequest().getHeaders();

        // Evaluamos según el comportamiento de tu JwtFilter de producción
        assertEquals("richard", headersResultantes.getFirst("X-User-Name"));
        assertEquals("ROLE_ADMIN", headersResultantes.getFirst("X-User-Role"));
        assertEquals("richard", headersResultantes.getFirst("X-User-Id"));
    }

    @Test
    @DisplayName("Prueba 3: Si el token es inválido, corta el flujo y responde con un 401 Unauthorized")
    void testFiltrarTokenInvalidoRetorna401() {
        MockServerHttpRequest requestConTokenMalo = MockServerHttpRequest.get("/api/v2/bff/proyectos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token.completamente.invalido")
                .build();
        MockServerWebExchange exchangeConTokenMalo = MockServerWebExchange.from(requestConTokenMalo);

        Mono<Void> resultado = jwtFilter.filter(exchangeConTokenMalo, filterChain);

        StepVerifier.create(resultado).verifyComplete();
        
        assertEquals(HttpStatus.UNAUTHORIZED, exchangeConTokenMalo.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }
}