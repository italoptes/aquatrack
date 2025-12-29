FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/aquatrack-1.0-SNAPSHOT-jar-with-dependencies.jar /app/aquatrack.jar

EXPOSE 8000

CMD ["java", "-jar", "aquatrack.jar"]
