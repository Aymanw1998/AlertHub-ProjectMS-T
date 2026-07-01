FROM eclipse-temurin:26-jre

ARG JAR_FILE="target/*.jar"

COPY ${JAR_FILE} EvaluationMS.jar

ENTRYPOINT ["java", "-jar", "/EvaluationMS.jar"]