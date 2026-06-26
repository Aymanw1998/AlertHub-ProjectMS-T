FROM eclipse-temurin:26-jre

ARG JAR_FILE="target/*.jar"

COPY ${JAR_FILE} EmailMS.jar

ENTRYPOINT ["java", "-jar", "/EmailMS.jar"]