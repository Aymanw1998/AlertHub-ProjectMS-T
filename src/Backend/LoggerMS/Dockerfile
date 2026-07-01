FROM eclipse-temurin:26-jre

ARG JAR_FILE="target/*.jar"

COPY ${JAR_FILE} LoggerMS.jar

ENTRYPOINT ["java", "-jar", "/LoggerMS.jar"]