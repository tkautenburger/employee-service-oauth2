#!/bin/sh

#      -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \

echo "******************************************************************"
echo "Reference Architecture - Starting Service"
echo "Application Name: $APP_NAME"
echo "HTTPS Port:       $HTTPS_SERVER_PORT"
echo "HTTP  Port:       $HTTP_SERVER_PORT"
echo "******************************************************************"

java -Dspring.profiles.active=$PROFILE \
	 -Dspring.application.name=$APP_NAME \
     -Dserver.port=$HTTP_SERVER_PORT \
     -Dlegendlime.https-port=$HTTPS_SERVER_PORT \
     -Djavax.net.ssl.trustStore=$TRUST_STORE_PATH \
     -Djavax.net.ssl.trustStorePassword=$TRUST_STORE_PASSWORD \
     -Djavax.net.ssl.keyStore=$KEY_STORE_PATH \
     -Djavax.net.ssl.keyStorePassword=$KEY_STORE_PASSWORD \
	 $HEAP_OPTIONS \
	 -jar /usr/local/employeeservice/EmployeeService-OAuth2-0.1-DRAFT.jar
