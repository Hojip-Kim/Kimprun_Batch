FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Gradle wrapper 파일 먼저 복사
COPY gradlew .
COPY gradle gradle
RUN chmod +x ./gradlew

# 의존성 캐싱을 위해 build.gradle과 settings.gradle 먼저 복사
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon || true

# 나머지 소스 코드 복사
COPY src src

# 빌드 실행
RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
