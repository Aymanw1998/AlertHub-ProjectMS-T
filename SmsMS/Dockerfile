FROM eclipse-temurin:26-jre

ARG JAR_FILE="target/*.jar"

COPY ${JAR_FILE} SmsMS.jar

ENTRYPOINT ["java", "-jar", "/SmsMS.jar"]