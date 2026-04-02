# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src src

# Install Maven
RUN apk add --no-cache maven

# Build the application (skip tests — they need DB)
RUN mvn package -DskipTests -q

# Stage 2: Runtime (slim image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# JVM tuning for free-tier containers (512MB RAM)
ENV JAVA_OPTS="-Xmx384m -Xms256m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
