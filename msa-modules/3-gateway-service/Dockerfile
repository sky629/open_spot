FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy gradle files
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Copy common modules first
COPY msa-modules/0-common-core msa-modules/0-common-core

# Copy gateway service
COPY msa-modules/3-gateway-service msa-modules/3-gateway-service

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew :3-gateway-service:build -x test --no-daemon

# Expose port
EXPOSE 8080

# Run the application with development profile
CMD ["java", "-Dspring.profiles.active=dev", "-jar", "msa-modules/3-gateway-service/build/libs/gateway-service-1.0.0.jar"]