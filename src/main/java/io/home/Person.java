package io.home;
import java.util.*;
import org.apache.kafka.clients.producer.*;

public class Person {
public static void main ( String [] args ){
 Properties props = new Properties();
 props.put("bootstrap.servers", "localhost:9092");
 props.put("acks", "all");
 props.put("retries", 0);
 props.put("batch.size", 16384);
 props.put("linger.ms", 1);
 props.put("buffer.memory", 33554432);
 props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
 props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

 Producer<String, String> producer = new KafkaProducer<>(props);
     producer.send(new ProducerRecord<String, String>("my-topic", "PalUSA", "CA"));
     producer.send(new ProducerRecord<String, String>("my-order", "PalUSA", "Currently Learning More Stuff"));
 producer.close();
}
}
