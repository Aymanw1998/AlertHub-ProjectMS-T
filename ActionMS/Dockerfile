FROM eclipse-temurin:26-jre

ARG JAR_FILE="target/*.jar"

COPY ${JAR_FILE} ActionMS.jar

ENTRYPOINT ["java", "-jar", "/ActionMS.jar"]