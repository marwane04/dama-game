FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom and source
COPY pom.xml .
COPY src ./src

# Build, skipping JavaFX-dependent tests and the javafx plugin
RUN mvn clean package -DskipTests \
    --no-transfer-progress \
    -Dmaven.javafx.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/target/dama-game-1.0-SNAPSHOT.jar app.jar

# Railway injects PORT automatically
ENV PORT=1234

EXPOSE 1234

CMD ["java", "-cp", "app.jar", "com.dama.network.Server"]