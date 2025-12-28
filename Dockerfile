# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the Java project
COPY my-app ./my-app
WORKDIR /app/my-app

RUN mvn clean package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/my-app/target /app/target

EXPOSE 8080

CMD ["java", "-cp", "target/*:target/dependency/*", "com.example.Main"]
