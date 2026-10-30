FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY sbp-lib-common ./sbp-lib-common
RUN mvn -q -f sbp-lib-common/pom.xml install
WORKDIR /app
COPY sbp-studio-service/pom.xml ./
COPY sbp-studio-service/src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
