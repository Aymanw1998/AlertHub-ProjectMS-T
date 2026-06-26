FROM eclipse-temurin:26-jre

ARG JAR_FILE="target/*.jar"

COPY ${JAR_FILE} UserMS.jar

ENTRYPOINT ["java", "-jar", "/UserMS.jar"]