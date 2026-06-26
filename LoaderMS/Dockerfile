FROM eclipse-temurin:26-jre

ARG JAR_FILE="target/*.jar"

COPY ${JAR_FILE} LoaderMS.jar

ENTRYPOINT ["java", "-jar", "/LoaderMS.jar"]