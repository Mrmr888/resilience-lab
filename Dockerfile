# syntax=docker/dockerfile:1
FROM maven:3.9.11-eclipse-temurin-21 AS build

ARG MODULE
WORKDIR /workspace

COPY pom.xml ./
COPY service-registry/pom.xml service-registry/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
RUN mvn -B -pl "${MODULE}" -am dependency:go-offline

COPY service-registry/src service-registry/src
COPY api-gateway/src api-gateway/src
COPY inventory-service/src inventory-service/src
COPY order-service/src order-service/src
RUN mvn -B -pl "${MODULE}" -am -DskipTests package

FROM eclipse-temurin:21-jre

ARG MODULE
WORKDIR /app
RUN useradd --system --uid 10001 appuser
COPY --from=build "/workspace/${MODULE}/target/${MODULE}-0.1.0-SNAPSHOT.jar" /app/app.jar

USER appuser
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

