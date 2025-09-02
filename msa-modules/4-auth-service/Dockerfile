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

# Copy auth service
COPY msa-modules/4-auth-service msa-modules/4-auth-service

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew :4-auth-service:build -x test --no-daemon

# Expose port
EXPOSE 8081

# Run the application with development profile
CMD ["java", "-Dspring.profiles.active=dev", "-jar", "msa-modules/4-auth-service/build/libs/auth-service-1.0.0.jar"]