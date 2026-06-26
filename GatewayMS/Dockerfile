FROM eclipse-temurin:26-jre

ARG JAR_FILE="target/*.jar"

COPY ${JAR_FILE} GatewayMS.jar

ENTRYPOINT ["java", "-jar", "/GatewayMS.jar"]