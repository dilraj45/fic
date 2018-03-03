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

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Utility class for Apache Kafka
 *
 * @author dilraj45
 */
public class KafkaUtils {

    private static Properties kafkaUtilsConfig;

    /**
     * A static method that can be invoked to obtain default configurations for {@link KafkaUtils}
     *
     * @return {@link Properties} for {@link KafkaUtils}
     */
    public static Properties getDefaultKafkaUtilsConfig() {
        if (KafkaUtils.kafkaUtilsConfig != null) {
            return KafkaUtils.kafkaUtilsConfig;
        }
        Properties props = new Properties();
        try (FileInputStream configFile = new FileInputStream(
                "src/main/java/com/yrf/dilraj/config/kafkaUtilsConfig.properties")) {
            props.load(configFile);
            return props;
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            throw new RuntimeException("FileNotFoundException");
        } catch (IOException exception) {
            exception.printStackTrace();
            throw new RuntimeException("Failed to load config Properties");
        }
    }

    /**
     * A wrapper class over {@link KafkaProducer} that can be used to specify configurations and for publishing
     * messages to particular Kafka topic
     *
     * @param <K> Type for key for kafka record
     * @param <V> Type for value for kafka record
     */
    public static class KafkaProducerUtils<K, V>  implements Closeable {

        @Getter @Setter private KafkaProducer<K,V> producerClient;

        public static Logger LOGGER = LoggerFactory.getLogger(KafkaProducerUtils.class);

        /**
         * This method can be used to obtain an instance of {@link KafkaProducer} with default configuration
         *
         * @return {@link KafkaProducer}
         */
        public static KafkaProducer createKafkaProducerClient() {
            return createKafkaProducerClient(getDefaultKafkaUtilsConfig());
        }

        /**
         * This method can be used to obtain an instance of {@link KafkaProducer} with {@link Properties} passed as an
         * argument to this method
         *
         * @param props of type {@link Properties}, that define the configurations for {@link KafkaProducer}
         * @return {@link KafkaProducer}
         */
        public static KafkaProducer createKafkaProducerClient(Properties props) {
            return new KafkaProducer<>(props);
        }

        public KafkaProducerUtils(KafkaProducer<K, V> producerClient) {
            this.producerClient = producerClient;
        }

        /**
         * This method can be invoked to publish a message to a particular Kafka topic passed as an argument to this
         * method. Message to be published comprise of key and value which are also passed to this method as an argument
         *
         * @param TOPIC of type {@link String} depicting the name of kafka topic to which the result is to published
         * @param key of type {@link K} depicting the key for kafka record to published
         * @param message of type {@Link V} depicting the value for kafka record to published
         * @return {@link Future} object of type {@link RecordMetadata}
         */
        public Future<RecordMetadata> sendMessage(final String TOPIC, K key, V message) {
            ProducerRecord<K, V> record = new ProducerRecord<>(TOPIC, key, message);
            return this.producerClient.send(record, (RecordMetadata metadata, Exception exception) ->
                    LOGGER.info("sent record(key=%s value=%s) " +
                                    "meta(partition=%d, offset=%d)\n",
                            record.key(), record.value(), metadata.partition(),
                            metadata.offset())
                );
        }

        /**
         * This method can be invoked to publish a message to a particular Kafka topic passed as an argument to this
         * method. Message to be published comprise of key and value which are also passed to this method as an argument
         * Additionally, you can also specify a {@link Callback} that is invoked when the message has been successfully
         * published to kafka topic.
         *
         * @param TOPIC of type {@link String} depicting the name of kafka topic to which the result is to published
         * @param key of type {@link K} depicting the key for kafka record to published
         * @param message of type {@Link V} depicting the value for kafka record to published
         * @param callback of type {@Callback callback} that is invoked upon completion of send operation
         * @return {@link Future} object of type {@link RecordMetadata}
         * @return
         */
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

    /**
     * A wrapper class for {@link KafkaConsumer} that can be used for configuring {@link KafkaConsumer} and also
     * facilitates consumption of {@link ConsumerRecords} from given topic
     *
     * @param <K> Type for key for kafka record
     * @param <V> Type for value for kafka record
     */
    public static class KafkaConsumerUtils<K, V> implements Closeable {

        @Getter private Consumer<K, V> consumerClient;

        public static Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerUtils.class);

        public KafkaConsumerUtils() {
            this(getDefaultKafkaUtilsConfig());
        }

        /**
         * Constructor to create an instance of KafkaConsumer with given {@link Properties}
         *
         * @param props {@link Properties}
         */
        public KafkaConsumerUtils(Properties props) {
            this.consumerClient = new KafkaConsumer<>(props);
        }

        public void subscribe(final Collection<String> TOPIC) {
            consumerClient.subscribe(TOPIC);
        }

        public List<Pair<K, V>> poll(long timeout) {
            ConsumerRecords<K, V> records = this.consumerClient.poll(timeout);
            List<Pair<K, V>>receivedRecords = new LinkedList<>();
            for (ConsumerRecord<K, V> record: records) {
                Pair recievedRecord = new Pair<K, V>(record.key(), record.value());
                LOGGER.info("Offset %s, and listening to partition %s", record.offset(), record.partition());
                receivedRecords.add(recievedRecord);
            }
            return receivedRecords;
        }

        @Override
        public void close() throws IOException {
            this.consumerClient.close();
        }
    }

}
