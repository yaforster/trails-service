# Build stage: compile and package the Spring Boot jar from source.
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /workspace

RUN apt-get update && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

COPY pom.xml ./
COPY src/ src/
COPY lombok.config ./

RUN mvn -DskipTests clean package

FROM eclipse-temurin:25-jre-noble

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
