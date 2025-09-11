FROM openjdk:17-slim

WORKDIR /app

COPY build/libs/ktor-all.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]