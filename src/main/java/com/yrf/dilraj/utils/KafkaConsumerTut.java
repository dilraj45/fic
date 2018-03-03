package com.yrf.dilraj.utils;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerTut {

    private static Consumer<Long, String> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "KafkaExampleConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "tut_group");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        final Consumer<Long, String> consumer = new KafkaConsumer<Long, String>(props);
        consumer.subscribe(Collections.singletonList("tut"));
        return consumer;
    }

    public static void runConsumer() throws Exception {
        final Consumer<Long, String> consumer = createConsumer();
        while (true) {
            ConsumerRecords<Long, String> records = consumer.poll(100);
            for (ConsumerRecord record : records) {
                System.out.println("Record Key: " + record.key());
                System.out.println("Record Value: " + record.value());
                System.out.println("Commited offset " + record.offset());
                System.out.println("Listening to partition: " + record.partition());
                System.out.println("---------------------------------------------------");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Consumer---------------");
        runConsumer();
        System.out.println("Exiting Consumer----------------");
    }

}
