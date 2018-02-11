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

import io.confluent.examples.connectandstreams.utils.GenericAvroSerde;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

/**
 * Demonstrates how to perform a join between a KStream and a KTable, i.e. an example of a stateful
 * computation, using the generic Avro binding for serdes in Kafka Streams. Same as
 * PageViewRegionLambdaExample but does not use lambda expressions and thus works on Java 7+.
 *
 * In this example, we join a stream of page views (aka clickstreams) that reads from a topic named
 * "PageViews" with a user profile table that reads from a topic named "UserProfiles" to compute the
 * number of page views per user region.
 *
 * Note: The generic Avro binding is used for serialization/deserialization.  This means the
 * appropriate Avro schema files must be provided for each of the "intermediate" Avro classes, i.e.
 * whenever new types of Avro objects (in the form of GenericRecord) are being passed between
 * processing steps.
 *
 * HOW TO RUN THIS EXAMPLE
 *
 * 1) Start Zookeeper, Kafka, and Confluent Schema Registry. Please refer to <a
 * href='http://docs.confluent.io/3.0.0/quickstart.html#quickstart'>CP3.0.0 QuickStart</a>.
 *
 * 2) Create the input/intermediate/output topics used by this example.
 *
 * <pre>
 * {@code
 * $ bin/kafka-topics --create --topic PageViews \
 *                    --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 * $ bin/kafka-topics --create --topic PageViewsByUser \
 *                    --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 * $ bin/kafka-topics --create --topic UserProfiles \
 *                    --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 * $ bin/kafka-topics --create --topic PageViewsByRegion \
 *                    --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 * }
 * </pre>*
 *
 * Note: The above commands are for CP 3.0.0 only. For Apache Kafka it should be
 * `bin/kafka-topics.sh ...`.
 *
 * 3) Start this example application either in your IDE or on the command line.
 *
 * If via the command line please refer to <a href='https://github.com/confluentinc/examples/tree/master/kafka-streams#packaging-and-running'>Packaging</a>.
 * Once packaged you can then run:
 *
 * <pre>
 * {@code
 * $ java -cp target/streams-examples-3.0.0-standalone.jar io.confluent.examples.streams.PageViewRegionLambdaExample
 * }
 * </pre>
 *
 * 4) Write some input data to the source topics (e.g. via {@link PageViewRegionExampleDriver}). The
 * already running example application (step 3) will automatically process this input data and write
 * the results to the output topic.
 *
 * <pre>
 * {@code
 * # Here: Write input data using the example driver.  Once the driver has stopped generating data,
 * # you can terminate it via `Ctrl-C`.
 * $ java -cp target/streams-examples-3.0.0-standalone.jar io.confluent.examples.streams.PageViewRegionExampleDriver
 * }
 * </pre>
 *
 * 5) Inspect the resulting data in the output topic, e.g. via `kafka-console-consumer`.
 *
 * <pre>
 * {@code
 * $ bin/kafka-console-consumer --zookeeper localhost:2181 --topic PageViewsByRegion
 * --from-beginning \
 *          --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
 * }
 * </pre>
 *
 * You should see output data similar to:
 *
 * <pre>
 * {@code
 * [africa@1466515140000]  1
 * [africa@1466515080000]  1
 * [africa@1466514900000]  1
 * [africa@1466515020000]  1
 * [africa@1466514960000]  1
 * [africa@1466514900000]  2
 * [africa@1466514960000]  2
 * [africa@1466515020000]  2
 * [africa@1466515080000]  2
 * [africa@1466515140000]  2
 * [asia@1466515140000]  1
 * [asia@1466515080000]  1
 * [asia@1466514900000]  1
 * [asia@1466515020000]  1
 * [asia@1466514960000]  1
 * [asia@1466514900000]  2
 * [asia@1466514960000]  2
 * [asia@1466515020000]  2
 * [asia@1466515080000]  2
 * [asia@1466515140000]  2
 * [asia@1466514900000]  3
 * ...
 * }
 * </pre>
 *
 * Here, the output format is "[REGION@WINDOW_START_TIME] COUNT".
 *
 * 6) Once you're done with your experiments, you can stop this example via `Ctrl-C`.  If needed,
 * also stop the Confluent Schema Registry (`Ctrl-C`), then stop the Kafka broker (`Ctrl-C`), and
 * only then stop the ZooKeeper instance (`Ctrl-C`).
 */
public class MyAvroConsumer {

  public static void main(String[] args) throws Exception {
    Properties streamsConfiguration = new Properties();
    // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
    // against which the application is run.
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "pageview-region-example");
    // Where to find Kafka broker(s).
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    // Where to find the corresponding ZooKeeper ensemble.
    streamsConfiguration.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "localhost:2181");
    // Where to find the Confluent schema registry instance(s)
    streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
    // Specify default (de)serializers for record keys and for record values.
    //streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    final Serde<String> stringSerde = Serdes.String();
    final Serde<Long> longSerde = Serdes.Long();

    KStreamBuilder builder = new KStreamBuilder();

    // Create a stream of page view events from the PageViews topic, where the key of
    // a record is assumed to be the user id (String) and the value an Avro GenericRecord
    // that represents the full details of the page view event.  See `pageview.avsc` under
    // `src/main/avro/` for the corresponding Avro schema.
    KStream<String, GenericRecord> views = builder.stream("mysql-foobar");
views.print();

    final KStream<String, GenericRecord> viewsByUser = views
      .map((dummy, record) ->
        new KeyValue<>(record.get("c1").toString(), record));
viewsByUser.print();

    // write to the result topic

    KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);
    streams.start();
  }

}
