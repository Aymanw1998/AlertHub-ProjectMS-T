FROM eclipse-temurin:26-jre

ARG JAR_FILE="target/*.jar"

COPY ${JAR_FILE} ProcessorMS.jar

ENTRYPOINT ["java", "-jar", "/ProcessorMS.jar"]