FROM openjdk:8-jre-alpine

ADD kafka-server.properties /kafka_2.11-1.0.0/config/

RUN apk update && apk add bash wget \
	&& wget http://www-eu.apache.org/dist/kafka/1.0.0/kafka_2.11-1.0.0.tgz \
	&& tar -xzf kafka_2.11-1.0.0.tgz

WORKDIR /kafka_2.11-1.0.0

CMD ["bin/kafka-server-start.sh","config/kafka-server.properties"]
