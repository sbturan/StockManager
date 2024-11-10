FROM openjdk:17
WORKDIR /my-project
CMD ["./gradlew", "clean","build","bootJar"]
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar","-web -webAllowOthers -tcp -tcpAllowOthers -browser"]