FROM eclipse-temurin:21-jdk-alpine
MAINTAINER ushki.dev
COPY target/live-dnd-list-0.0.1-SNAPSHOT.jar live-dnd-list-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/live-dnd-list-0.0.1-SNAPSHOT.jar"]

