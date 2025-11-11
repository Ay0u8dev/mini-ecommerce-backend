# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17-alpine AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /build/target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8084

HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]