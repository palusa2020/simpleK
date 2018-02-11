# simpleK
<pre>
Maven clean , Put and Get Data.

For version 0.10

mvn clean install
java -cp target/home_program-0.0.1-SNAPSHOT-jar-with-dependencies.jar io.home.PutData
java -cp target/home_program-0.0.1-SNAPSHOT-jar-with-dependencies.jar io.home.GetData


For version 1.0

mvn clean install
java -cp target/home_program-0.0.1-SNAPSHOT-jar-with-dependencies.jar io.home.PutData
java -cp target/home_program-0.0.1-SNAPSHOT-jar-with-dependencies.jar io.home.GetData


For version 1.0
mvn clean install
java -cp target/home_program-0.0.1-SNAPSHOT-jar-with-dependencies.jar io.home.Person
java -cp target/home_program-0.0.1-SNAPSHOT-jar-with-dependencies.jar io.home.PersonStream

Output:
~/simpleK$ java -cp target/home_program-0.0.1-SNAPSHOT-jar-with-dependencies.jar io.home.PersonStream 
log4j:WARN No appenders could be found for logger (org.apache.kafka.streams.StreamsConfig).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
[KSTREAM-JOIN-0000000004]: PalUSA, Currently Learning Deeplearning
[KSTREAM-JOIN-0000000004]: PalUSA, Currently Learning More Stuff
[KSTREAM-JOIN-0000000004]: PalUSA, Currently Learning More Stuff

The string "PalUSA" is the key , this is an example of stream to ktable join.

</pre>
