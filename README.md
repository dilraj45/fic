
# FIC Focused distributed Intelligent Crawling System

FIC is focused, distributed and an intelligent crawling system. FIC is a focused crawler that will crawl pages related 
to given domain (as specified by the user). FIC is different from other crawler in the sense that it takes the relevance 
of web-pages into consideration for determining the crawl order for web-pages. Relevance of web-pages can be computed on 
the basis of simple heuristics or may be using some machine learning models.

As this project is still in development stage, support for page classifiers and ability to run on multiple machine is 
yet to included. At present it supports running the crawl job in multiple docker container on a single machine without 
any preference crawl order.

## Installation

Downloader-service is the one responsible for downloading the web content for a given URL. It uses Apache Kafka streams 
for queueing crawl jobs. You can either run instance of downloader service by separately installing Apache Kafka Stream 
on your system or by simply running `docker-compose up` with `..\fic\` as your present working directory. Docker compose 
will automatically spin up a kafka server for you, and will instantiate a container for downloader service.

**Prerequisite** You will need a working installation of Oracle Java-8

### Running an instance of downloader service with Apache Kafka server pre installed on system
You can configure the host name for bootstrap servers by configuring the value for `bootstrap.server` in 
`kafkaUtilsConfig.properties`. To build FIC from source, you can execute the following commands in your terminal
```
git clone https://github.com/dilraj45/fic.git
cd fic
./gradlew build
```

To run an instance of downloader service you can execute `./gradlew run`

### Using Docker
You can bypass the onerous process of installing Apache Kafka and it's dependencies by simply doing a `docker-compose up`. If you wish to run a single container for service container (You can always adjust the number of container by 
configuring it in docker-compose.yml file) execute the following commands on your terminal
```
git clone https://github.com/dilraj45/fic.git
cd fic
docker-compose up
```

*NOTE: Current version does not have any means to specify the starting seed URLs for the crawl. It has to be pushed 
manually to Kafka Stream to initiate crawling. To manually push messages to Kafka Stream execute the following 
instructions in your terminal. Further, if you are using docker to run service, you will need to log into container that 
is running kafka-server. You can do that by executing* `docker exec -it xxxContainer-Id /bin/bash/`.
```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic my-topic
>
```


