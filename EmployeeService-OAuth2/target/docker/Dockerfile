FROM openjdk:12-jdk-alpine
RUN apk update && apk upgrade && apk add netcat-openbsd
RUN mkdir -p /usr/local/employeeservice
ADD employeeservice-oauth2-0.1-SNAPSHOT.jar /usr/local/employeeservice/
ADD run.sh run.sh
RUN chmod +x run.sh
CMD ./run.sh
