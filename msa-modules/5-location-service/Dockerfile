FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy gradle files
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Copy common modules first
COPY msa-modules/0-common-core msa-modules/0-common-core
COPY msa-modules/1-common-web msa-modules/1-common-web

# Copy location service
COPY msa-modules/5-location-service msa-modules/5-location-service

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew :5-location-service:build -x test --no-daemon

# Expose port
EXPOSE 8082

# Run the application with development profile
CMD ["java", "-Dspring.profiles.active=dev", "-jar", "msa-modules/5-location-service/build/libs/location-service-1.0.0.jar"]