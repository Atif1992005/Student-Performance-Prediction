# Multi-stage Dockerfile for Spring Boot Backend (Java 21)

# Stage 1: Build JAR using Maven
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Pre-fetch Maven dependencies to maximize layer cache efficiency
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy Java source code and compile production JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal, secure JRE 21 Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-privileged system user for container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Adjust ownership
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

# Faster startup and random seed initialization
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
