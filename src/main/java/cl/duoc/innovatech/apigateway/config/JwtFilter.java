package cl.duoc.innovatech.apigateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements WebFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String authHeader = request.getHeaders().getFirst("Authorization");

        // Si no viene header Authorization o no es Bearer, dejamos pasar.
        // Esto permite que las rutas públicas sigan funcionando sin token.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        try {
            var claims = jwtUtil.validateAndGetClaims(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

        // Modificamos la petición para meterle el usuario y el role en headers
        // Así los microservicios atrás no tienen que validar el JWT otra vez.
            ServerHttpRequest mutated = request.mutate()
                    .header("X-User-Name", username != null ? username : "")
                    .header("X-User-Role", role != null ? role : "")
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutated).build();
            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            // Si la validación falla, respondemos 401 para que el cliente sepa
            // que el token es inválido o expiró. No propagamos la petición.
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }
}
