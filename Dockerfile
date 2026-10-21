FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
ARG APP_MODULE
ARG APP_JAR_PATH
RUN mvn -B -ntp -pl "${APP_MODULE}" -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S -G app app
WORKDIR /app
ARG APP_JAR_PATH
COPY --from=build --chown=app:app /workspace/${APP_JAR_PATH} /app/app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
