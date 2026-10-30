FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY sbp-migration/pom.xml ./
COPY sbp-migration/src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/target/sbp-migration-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
