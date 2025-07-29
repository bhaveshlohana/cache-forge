# Stage 1: Build the application
# Using a Maven image with JDK 21 as per your pom.xml
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy the Maven project files (pom.xml and src)
COPY pom.xml .
COPY src ./src

# Build the Spring Boot application, skipping tests to speed up image build
# The target JAR will be named 'cache-forge-0.0.1-SNAPSHOT.jar' based on pom.xml artifactId and version
RUN mvn clean install -DskipTests

# Stage 2: Create the final image
# Using a Temurin JRE 21 alpine image for a smaller runtime footprint
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/cache-forge-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot application runs on
EXPOSE 8080

# Define the entry point to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]