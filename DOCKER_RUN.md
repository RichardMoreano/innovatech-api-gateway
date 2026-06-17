# Guía de Ejecución con Docker - API Gateway V2

Este componente actúa como el punto de entrada perimetral único (Reverse Proxy y API Gateway) del ecosistema **Innovatech**, encargado del enrutamiento reactivo, la gestión de CORS y la seguridad perimetral.

## 1. Prerrequisitos y Dependencias
El correcto funcionamiento de este contenedor depende de la disponibilidad de la red interna del ecosistema y los servicios secundarios:
* **Docker Engine** v20.10+ y **Docker Compose** v2.0+
* **Red de Docker:** `innovatech-net` (creada automáticamente por el orquestador principal).
* **Dependencias de Red:** El componente requiere que el contenedor `innovatech-bff` esté en estado *healthy* para resolver las rutas `/api/v2/bff/**`.

## 2. Puertos y Mapeo de Red
* **Puerto Interno (Contenedor):** `8083`
* **Puerto Externo (Host):** `8083`
* **Protocolo:** HTTP/1.1 (Spring Cloud Gateway Reactivo)

---

## 3. Comandos de Operación

### Ejecución en Primer Plano (Logs en vivo)
Para levantar el servicio construyendo la imagen limpia sin caché de capas previas:
```bash
docker compose up --build innovatech-api-gateway