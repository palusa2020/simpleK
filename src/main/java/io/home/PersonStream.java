package io.home;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.kafka.streams.kstream.Joined;
public class PersonStream {
  public static void main(final String[] args) throws Exception {
    final String bootstrapServers = "localhost:9092";
    final Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-lambda-example");
    streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "wordcount-lambda-example-client");
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
    streamsConfiguration.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
    final Serde<String> stringSerde = Serdes.String();
    final Serde<Long> longSerde = Serdes.Long();
    final KStreamBuilder builder = new KStreamBuilder();
    final KStream<String, String> textLines = builder.stream(stringSerde, stringSerde, "my-topic");
    final KTable<String, String> currentLearning = builder.table(stringSerde, stringSerde, "my-order");
    //textLines.print();

/*
    final Pattern pattern = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);
    final KTable<String, Long> wordCounts = textLines
      .flatMapValues(value -> Arrays.asList(pattern.split(value.toLowerCase())))
      .groupBy((key, word) -> key)
      .count("Counts");
    //wordCounts.print();
    wordCounts.to(stringSerde, longSerde, "WordsWithCountsTopic");


System.out.println("Output of Join -->");
*/
/*
KStream<String, String> joined = textLines.join( wordCounts ,
 (leftValue, rightValue) -> "left=" + leftValue + ", right=" + rightValue, 
  Joined.keySerde(Serdes.String()) 
  );
*/
KStream<String, String> joined = textLines.join( currentLearning ,
 (leftValue, rightValue) -> rightValue );


joined.print();
/* Join of kstream andktable */


    final KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);
    streams.cleanUp();
    streams.start();
    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }
}
