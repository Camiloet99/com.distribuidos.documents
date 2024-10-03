# Etapa 1: Construir el proyecto
FROM openjdk:17-jdk-slim AS build

# Instalar Maven
RUN apt-get update && apt-get install -y maven

# Copiar el pom.xml y descargar las dependencias
COPY pom.xml /app/
COPY src /app/src/
WORKDIR /app

# Ejecutar Maven con verbose output y listar el contenido de target
RUN mvn clean package -DskipTests --debug && ls -l target/

# Etapa 2: Crear la imagen final
FROM openjdk:17-jdk-slim

# Crear un directorio para la aplicación
WORKDIR /app

# Copiar el JAR construido desde la etapa de construcción usando comodín
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto en el que la aplicación escucha
EXPOSE 8080

# Agregar al Dockerfile en la etapa final
# Agregar esto en tu Dockerfile
COPY src/main/resources/documentmanager-436319-ad4006b4fbc9.json /app/documentmanager-436319-ad4006b4fbc9.json

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/documentmanager-436319-ad4006b4fbc9.json

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]