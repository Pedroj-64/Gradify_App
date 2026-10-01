FROM gradle:8.9-jdk17 AS build
WORKDIR /src
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /src/build/libs/*.jar app.jar
ENV GEMINI_API_KEY="" APP_TOKEN=""
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
