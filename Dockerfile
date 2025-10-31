# Stage 1: сборка
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# Stage 2: запуск
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]