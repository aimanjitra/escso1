# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# copy project
COPY my-app/pom.xml ./my-app/pom.xml
COPY my-app/src ./my-app/src

WORKDIR /app/my-app
RUN mvn clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# copy the runnable jar
COPY --from=build /app/my-app/target/my-app-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
