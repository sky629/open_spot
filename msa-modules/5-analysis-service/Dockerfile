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

# Copy analysis service
COPY msa-modules/5-analysis-service msa-modules/5-analysis-service

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew :5-analysis-service:build -x test --no-daemon

# Expose port
EXPOSE 8082

# Run the application with development profile
CMD ["java", "-Dspring.profiles.active=dev", "-jar", "msa-modules/5-analysis-service/build/libs/analysis-service-1.0.0.jar"]