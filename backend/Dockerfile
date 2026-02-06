# Stage de build
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copia wrapper e configs do Gradle
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Copia o código-fonte
COPY src ./src

# Build do jar
RUN chmod +x ./gradlew && ./gradlew bootJar --no-daemon

# Stage final (runtime)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
