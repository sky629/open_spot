FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy gradle files
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Copy common modules first
COPY common-modules common-modules

# Copy config service
COPY msa-modules/2-config-service msa-modules/2-config-service

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew :2-config-service:build -x test --no-daemon

# Expose port
EXPOSE 9999

# Run the application with development profile
CMD ["java", "-Dspring.profiles.active=dev", "-jar", "msa-modules/2-config-service/build/libs/config-service-1.0.0.jar"]