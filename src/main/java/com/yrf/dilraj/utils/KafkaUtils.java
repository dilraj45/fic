package com.yrf.dilraj.utils;

import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaUtils {

    public static class KafkaProducerUtils<K, V>  implements Closeable {

        @Getter @Setter private KafkaProducer<K,V> producerClient;

        public static Logger LOGGER = LoggerFactory.getLogger(KafkaProducerUtils.class);

        public static Properties getDefaultProducerConfig() {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream("src/main/java/com/yrf/dilraj/config/kafkaUtilsConfig.properties"));
                return props;
            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
                throw new RuntimeException("FileNotFoundException");
            } catch (IOException exception) {
                exception.printStackTrace();
                throw new RuntimeException("Failed to load config Properties");
            }
        }

        public static KafkaProducer createKafkaProducerClient() {
            return createKafkaProducerClient(getDefaultProducerConfig());
        }

        public static KafkaProducer createKafkaProducerClient(Properties props) {
            return new KafkaProducer<>(props);
        }

        public KafkaProducerUtils(KafkaProducer<K, V> producerClient) {
            this.producerClient = producerClient;
        }

        public Future<RecordMetadata> sendMessage(final String TOPIC, K key, V message) {
            ProducerRecord<K, V> record = new ProducerRecord<>(TOPIC, key, message);
            return this.producerClient.send(record, (RecordMetadata metadata, Exception exception) ->
                    LOGGER.info("sent record(key=%s value=%s) " +
                                    "meta(partition=%d, offset=%d)\n",
                            record.key(), record.value(), metadata.partition(),
                            metadata.offset())
                );
        }

        public Future<RecordMetadata> sendMessage(final String TOPIC, K key, V message, Callback callback) {
            ProducerRecord<K, V> record = new ProducerRecord<>(TOPIC, key, message);
            return this.producerClient.send(record, callback);
        }

        @Override
        public void close() throws IOException {
            this.producerClient.flush();
            this.producerClient.close();
        }
    }

    public static class KafkaConsumerUtils<K, V> implements Closeable {

        @Getter private Consumer<K, V> consumerClient;

        public static Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerUtils.class);

        public static Properties getDefaultConsumerConfig() {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream("src/main/java/com/yrf/dilraj/config/kafkaUtilsConfig.properties"));
                return props;
            } catch (FileNotFoundException exception){
                exception.printStackTrace();
                throw new RuntimeException("FileNotFoundException");
            } catch (IOException exception) {
                exception.printStackTrace();
                throw new RuntimeException("Failed to load config properties");
            }
        }

        public KafkaConsumerUtils() {
            this(getDefaultConsumerConfig());
        }

        public KafkaConsumerUtils(Properties props) {
            this.consumerClient = new KafkaConsumer<>(props);
        }

        public void subscribe(final Collection<String> TOPIC) {
            consumerClient.subscribe(TOPIC);
        }

        public List<Pair<K, V>> poll(long timeout) {
            ConsumerRecords<K, V> records = this.consumerClient.poll(timeout);
            List<Pair<K, V>>recievedRecords = new LinkedList<>();
            for (ConsumerRecord<K, V> record: records) {
                Pair recievedRecord = new Pair<K, V>(record.key(), record.value());
                LOGGER.info("Offset %s, and listening to partition %s", record.offset(), record.partition());
                recievedRecords.add(recievedRecord);
            }
            return recievedRecords;
        }

        @Override
        public void close() throws IOException {
            this.consumerClient.close();
        }
    }

}
