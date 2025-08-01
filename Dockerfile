# 빌드 전용
FROM eclipse-temurin:17-jdk AS build

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

# 실행 전용
FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드된 JAR 파일만 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# PROD 환경 설정 파일 사용
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
