FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy gradle files
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Copy common modules first
COPY msa-modules/0-common-core msa-modules/0-common-core

# Copy notification service
COPY msa-modules/6-notification-service msa-modules/6-notification-service

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew :6-notification-service:build -x test --no-daemon

# Expose port
EXPOSE 8083

# Run the application with development profile
CMD ["java", "-Dspring.profiles.active=dev", "-jar", "msa-modules/6-notification-service/build/libs/notification-service-1.0.0.jar"]