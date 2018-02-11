package io.home;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;
import java.util.Scanner;
public class PutData {
 public static void main(String[] argv)throws Exception {
 String topicName = "MYTOPIC1" ;
 String msg = "Message 1 Life on Mars";
 Properties configProperties = new Properties();
 configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
 configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.ByteArraySerializer");
 configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");

 org.apache.kafka.clients.producer.Producer producer = new KafkaProducer(configProperties);
 ProducerRecord<String, String> rec = new ProducerRecord<String, String>(topicName,msg);
//producer.send(new ProducerRecord<String, String>(topicName, "Message1","Life On Mars" ));

 producer.send(rec);
 producer.close();
 }
}
