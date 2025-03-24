FROM openjdk:17-jdk-slim
WORKDIR /app
RUN mkdir -p /app/logs
COPY target/br-web-app-*.jar /app/app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
