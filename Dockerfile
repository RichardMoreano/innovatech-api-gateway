# ====================== STAGE DE COMPILACIÓN ======================
FROM bellsoft/liberica-openjdk-alpine:25 AS builder
WORKDIR /app

# Instalar Maven de forma nativa sobre Alpine sin depender de tags externos
RUN apk add --no-cache maven

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# ====================== STAGE DE EJECUCIÓN ======================
FROM bellsoft/liberica-openjdk-alpine:25
WORKDIR /app

RUN apk add --no-cache curl && \
    addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /app/target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]