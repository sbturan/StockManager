FROM gradle:7.5.0-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build --no-daemon

FROM openjdk:17

COPY --from=build /home/gradle/src/build/libs/*.jar  app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar","-web -webAllowOthers -tcp -tcpAllowOthers -browser"]

