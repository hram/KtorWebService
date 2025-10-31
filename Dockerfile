# Stage 1: сборка
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

# Stage 2: запуск
FROM openjdk:17-jre-slim

WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]