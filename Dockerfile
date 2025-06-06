# Étape 1 : build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : run
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copier le JAR généré
COPY --from=build /app/target/*.jar app.jar

# Copier le fichier Firebase JSON dans le bon répertoire
COPY src/main/resources/stockwatch-34392-firebase-adminsdk-fbsvc-8b4e6c926f.json /app/stockwatch-34392-firebase-adminsdk-fbsvc-8b4e6c926f.json

# Optionnel : définir le chemin dans une variable d’environnement
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/stockwatch-34392-firebase-adminsdk-fbsvc-8b4e6c926f.json

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
