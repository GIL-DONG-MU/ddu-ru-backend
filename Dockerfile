# Build stage
FROM openjdk:17-jdk-slim AS build

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew ./
COPY gradle gradle/
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src src/

# Build the application
RUN ./gradlew clean build -x test

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Set environment variables for production
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]