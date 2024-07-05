FROM maven:3.8.1-jdk-11-slim AS build
WORKDIR /app
COPY . /app
RUN mvn clean package

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/FlinkStateQueryClient-1.0-SNAPSHOT.jar /app/FlinkStateQueryClient-1.0-SNAPSHOT.jar
ENTRYPOINT ["java", "-cp", "FlinkStateQueryClient-1.0-SNAPSHOT.jar", "com.platformatory.RestServer"]
