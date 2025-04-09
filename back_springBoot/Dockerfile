FROM openjdk:17-jdk

WORKDIR /app

COPY target/*SNAPSHOT.jar app.jar

EXPOSE 8002

ENTRYPOINT [ "java", "-jar", "app.jar" ]