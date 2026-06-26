FROM eclipse-temurin:26-jre

ARG JAR_FILE="target/*.jar"

COPY ${JAR_FILE} MetricMS.jar

ENTRYPOINT ["java", "-jar", "/MetricMS.jar"]