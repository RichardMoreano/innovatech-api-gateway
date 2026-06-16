# Innovatech API Gateway

Este módulo implementa el API Gateway usando Spring Cloud Gateway y un filtro reactivo que valida JWT y agrega headers X-User-Name y X-User-Role para que los microservicios confíen en el gateway.

Puerto por defecto: 9000

Rutas configuradas en `application.properties`:
- /api/bff/** -> http://localhost:8080
- /api/proyectos/** -> http://localhost:8081
- /api/recursos/** -> http://localhost:8082

