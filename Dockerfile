FROM eclipse-temurin:18-jre

ARG SERVICE_NAME

ENV SERVICE_NAME=$SERVICE_NAME

WORKDIR /app

COPY $SERVICE_NAME/target/$SERVICE_NAME-0.0.1-SNAPSHOT.jar /app/

CMD java -jar $SERVICE_NAME-0.0.1-SNAPSHOT.jar