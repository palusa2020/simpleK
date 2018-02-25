package io.home;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.apache.kafka.streams.kstream.Windowed;
import java.io.InputStream;
import java.util.Properties;
import io.confluent.examples.connectandstreams.utils.*;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Produced;
import java.util.Locale;
import java.lang.String;
import org.apache.kafka.streams.kstream.Materialized;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.Joined;






public class MyAvroKstream2Ktable {
  public static void main(String[] args) throws Exception {
   final String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";
    final Properties streamsConfiguration = new Properties();
    // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
    // against which the application is run.
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "anomaly-detection-lambda-example");
    streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "anomaly-detection-lambda-example-client");
    // Where to find Kafka broker(s).
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    // Specify default (de)serializers for record keys and for record values.

        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        // Specify default (de)serializers for record keys and for record values.
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);

 StreamsBuilder builder = new StreamsBuilder();
    final KStream<String, GenericRecord> input = builder.stream("topic2");
input.print();
input.foreach((key, value) -> System.out.println(key + " => " + value.get("name") ));


input.to("tktable");

/*
make a ktable from topic
*/
final KTable<String, GenericRecord> kt = builder.table("tktable");

/*
    final KTable<String, String> kt = input.mapValues(record ->
      record.get("name").toString());
*/

kt.print();

KStream<String, String> kskt = input.join(kt,
    (leftValue, rightValue) -> "left=" + leftValue.get("name").toString() + ", right=" + rightValue.get("name").toString() 
  );
kskt.foreach((key, value) -> System.out.println(key + " => kskt=>" + value ));

KStream<String, GenericRecord> kskt2 = input.join(kt,
    (leftValue, rightValue) -> {
                                  return rightValue;
                               }
  );


    final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
    streams.cleanUp();
    streams.start();

  }
}
