# Etapa 1: compilar y empaquetar como Azure Function
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY host.json .
COPY local.settings.json .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package azure-functions:package -DskipTests

# Etapa 2: imagen oficial de Microsoft para Azure Functions Java 17
FROM mcr.microsoft.com/azure-functions/java:4-java17
ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true \
    AzureWebJobsStorage="" \
    FUNCTIONS_WORKER_RUNTIME=java
COPY --from=build /app/target/azure-functions/usuarios-function-app/ /home/site/wwwroot
EXPOSE 80
