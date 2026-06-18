# Innovatech API Gateway

Este módulo implementa el API Gateway del proyecto Innovatech usando Spring Cloud Gateway (stack reactivo - WebFlux).

Escrito en un estilo claro y sencillo para que cualquiera del equipo entienda qué hace, cómo funciona y cómo probarlo/desplegarlo.

## ¿Qué hace este módulo?

- Centraliza el enrutamiento de las peticiones HTTP hacia los microservicios (BFF, autenticación, recursos, proyectos, etc.).
- Valida tokens JWT en la periferia (perimeter security) mediante un filtro reactivo (`JwtFilter`) para evitar que cada microservicio valide el token por separado.
- En caso de JWT válido, inyecta información de identidad en headers HTTP (`X-User-Id`, `X-User-Roles`, `X-User-Name`, `X-User-Role`) para que los microservicios confíen en el gateway y puedan autorizar/registrar sin revalidar el token.
- Configura CORS y reglas básicas de seguridad reactivas con Spring Security WebFlux.

## Estructura principal

- `src/main/java/cl/duoc/innovatech/apigateway/ApiGatewayApplication.java` - Clase principal de Spring Boot.
- `src/main/java/cl/duoc/innovatech/apigateway/config/SecurityConfig.java` - Configuración de seguridad reactiva y CORS.
- `src/main/java/cl/duoc/innovatech/apigateway/config/JwtUtil.java` - Utilidades para validar/parsear JWT.
- `src/main/java/cl/duoc/innovatech/apigateway/config/JwtFilter.java` - Filtro reactivo que valida el token y agrega headers de identidad.
- `src/main/resources/application.properties` - Configuración de rutas (`spring.cloud.gateway.routes`), puerto, y secret JWT.

## Puertos y rutas (por defecto)

- Puerto por defecto: `8083`.
- Rutas definidas en `application.properties`:
    - `Path=/api/v2/bff/**` → Enruta al servicio BFF.
    - `Path=/api/v2/auth/**` → Enruta al servicio de autenticación.

Las URIs destino se leen desde variables como `BFF_SERVICE_URL` y `AUTH_SERVICE_URL` (ver sección Variables de entorno).

## Seguridad y JWT

- El filtro `JwtFilter` intercepta todas las peticiones entrantes y busca el header `Authorization` con el esquema `Bearer <token>`.
- Si el header no existe o no tiene el esquema `Bearer`, la petición se deja pasar (esto permite endpoints públicos).
- Si existe un token, `JwtFilter` usa `JwtUtil` para validar y parsear las claims.
    - **En caso de token válido:** Se extraen `subject` (username) y la claim `role` en singular, agregándolos a la petición como headers:
        - `X-User-Name` (nombre de usuario)
        - `X-User-Role` (rol simple)
        - `X-User-Id` (id extraído del username)
        - `X-User-Roles` (rol duplicado para compatibilidad)
    - **En caso de token inválido o expirado:** El filtro responde con `401 Unauthorized` y detiene la cadena.

> [!IMPORTANT]
> Los microservicios esperan estos headers y confían en el gateway para la validación del token. Si necesitas que el ID sea numérico en vez de string, debes ajustar la lógica de inyección de claims en el microservicio de autenticación.

## Variables de entorno y configuración

Configurable vía `application.properties` o variables de entorno en Docker / Docker Compose:

- `server.port` - Puerto del gateway.
- `BFF_SERVICE_URL` - URL del servicio BFF (ej. `http://innovatech-bff:8080`).
- `AUTH_SERVICE_URL` - URL del servicio de autenticación.
- `jwt.secret` o `JWT_SECRET` - Secreto usado por `JwtUtil` para validar firmas JWT (inyectado directo en el componente).

Ejemplo (`docker-compose.yml`):

```yaml
environment:
  - BFF_SERVICE_URL=http://innovatech-bff:8080
  - AUTH_SERVICE_URL=http://innovatech-auth:8083
  - JWT_SECRET=panconqueso=paltadme2==duocuc2026