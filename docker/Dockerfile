# Multi-stage Dockerfile for Pathgram

FROM maven:3.9.4-eclipse-temurin-21 as build
WORKDIR /workspace
COPY pom.xml mvnw mvnw.cmd ./
COPY src ./src
RUN mvn -DskipTests package -q

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY --from=build /workspace/target/pathgram-0.0.1-SNAPSHOT.jar /app/pathgram.jar
EXPOSE 8080 5100
ENTRYPOINT ["java","-jar","/app/pathgram.jar"]

