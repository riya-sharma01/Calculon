# Build stage: compile and package the Spring Boot jar.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# Run stage: JRE only, so the image stays small.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Render tells the app its port via the PORT env var (application.properties reads it: server.port=${PORT:8080}).
EXPOSE 8080
# Cap the heap relative to the container's memory so a small Render instance isn't OOM-killed.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
