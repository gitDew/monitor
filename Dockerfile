FROM gradle:8.5-jdk17 AS build

WORKDIR /app

COPY . .

RUN ./gradlew clean bootJar

FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]