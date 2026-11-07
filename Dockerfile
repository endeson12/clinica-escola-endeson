FROM eclipse-temurin:21-jre-jammy@sha256:1eac8b8fee11ca7d6725a48bc5d41992ed1561504ebae25490d2d062847e8269
WORKDIR /app
COPY --chown=10001:10001 target/clinica-escola-0.0.1-SNAPSHOT.jar /app/portal.jar
USER 10001:10001
ENV JAVA_TOOL_OPTIONS="-XX:ActiveProcessorCount=2 -Xmx384m -Djava.net.preferIPv4Stack=true"
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/portal.jar"]
