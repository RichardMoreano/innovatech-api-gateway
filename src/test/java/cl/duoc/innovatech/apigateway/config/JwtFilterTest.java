package cl.duoc.innovatech.apigateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

class JwtFilterTest {

    private JwtFilter jwtFilter;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = Mockito.mock(JwtUtil.class);
        jwtFilter = new JwtFilter();
        org.springframework.test.util.ReflectionTestUtils.setField(jwtFilter, "jwtUtil", jwtUtil);
    }

    @Test
    void testFiltroSinCabeceraAutorizacion() {
        // Preparación: configuramos mocks para imitar el entorno reactivo
        ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
        ServerHttpRequest request = Mockito.mock(ServerHttpRequest.class);
        ServerHttpResponse response = Mockito.mock(ServerHttpResponse.class);
        WebFilterChain chain = Mockito.mock(WebFilterChain.class);

        // Cuando se pida la request/response devolvemos los mocks y no hay header Authorization
        Mockito.when(exchange.getRequest()).thenReturn(request);
        Mockito.when(exchange.getResponse()).thenReturn(response);
        Mockito.when(request.getHeaders()).thenReturn(new HttpHeaders());
        Mockito.when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Ejecución: aplicamos el filtro
        Mono<Void> result = jwtFilter.filter(exchange, chain);
        result.block();

        // Verificación: la cadena de filtros debe continuar y no debe establecerse 401
        Mockito.verify(chain).filter(exchange);
        Mockito.verify(response, Mockito.never()).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testFiltroConTokenInvalido() {
        // Preparación: mocks y header Authorization con token inválido
        ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
        ServerHttpRequest request = Mockito.mock(ServerHttpRequest.class);
        ServerHttpResponse response = Mockito.mock(ServerHttpResponse.class);
        WebFilterChain chain = Mockito.mock(WebFilterChain.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer bad.token.value");

        Mockito.when(exchange.getRequest()).thenReturn(request);
        Mockito.when(exchange.getResponse()).thenReturn(response);
        Mockito.when(request.getHeaders()).thenReturn(headers);

        // Simulamos que la validación del token lanza una excepción (token inválido)
        Mockito.when(jwtUtil.validateAndGetClaims(Mockito.anyString())).thenThrow(new RuntimeException("invalid"));

        // Cuando el filtro intente completar la respuesta, devolvemos un Mono vacío para el mock
        Mockito.when(response.setComplete()).thenReturn(Mono.empty());

        // Ejecución: aplicamos el filtro
        Mono<Void> result = jwtFilter.filter(exchange, chain);
        result.block();

        // Verificación: debe establecerse 401 y no debe continuar la cadena de filtros
        Mockito.verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        Mockito.verify(chain, Mockito.never()).filter(exchange);
    }
}