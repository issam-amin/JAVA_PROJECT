package org.example.clientside.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class kafkaConfig {

    static KafkaProducer<String, String> producer = null ;

    public static  KafkaProducer getKafkaConfig() {
        if (producer == null) {
            Properties props = new Properties();
            props.put("bootstrap.servers", "172.26.127.55:9092");
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            producer = new KafkaProducer<>(props);
        }

            return producer;
    }

    public static void sendLoginMessage(String email, String username) {
       String TOPIC = "test-topic";

        String message = "User: " + username +  " email :" + email +" logged in at " + System.currentTimeMillis();

        // Send login message to Kafka
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, username, message);
        producer.send(record);

        System.out.println("Login message sent: " + message);

    }
    public static void sendReclamtionMessage(String reclamation , String type) {
        String TOPIC = "test-topic";

        String message =  reclamation +  "," + type +"," + System.currentTimeMillis();

        // Send login message to Kafka
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, "test", message);
        producer.send(record);

        System.out.println("claim message sent: " + message);

    }

    }





