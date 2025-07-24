# Etapa build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src/
RUN mvn clean package -DskipTests

# Etapa runtime
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto dinámico de Render
EXPOSE $PORT

# Configurar el comando de inicio para usar el puerto de Render
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
