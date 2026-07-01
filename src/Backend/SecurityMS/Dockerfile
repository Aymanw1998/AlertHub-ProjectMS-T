FROM eclipse-temurin:26-jre

ARG JAR_FILE="target/*.jar"

COPY ${JAR_FILE} SecurityMS.jar

ENTRYPOINT ["java", "-jar", "/SecurityMS.jar"]