# Build stage
FROM eclipse-temurin:21-jdk-noble AS builder
WORKDIR /build
COPY gradle ./gradle
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY src ./src
RUN ./gradlew clean bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-noble
WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]