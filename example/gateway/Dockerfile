FROM eclipse-temurin:17-jre-jammy

ARG JAR_FILE=gateway.jar
VOLUME /tmp
WORKDIR /
EXPOSE 19999
COPY ${JAR_FILE} /app.jar
ENTRYPOINT ["sh","-c","java ${JAVA_OPTS:-} -jar /app.jar"]
