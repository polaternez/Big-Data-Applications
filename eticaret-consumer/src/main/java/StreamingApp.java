import com.mongodb.spark.MongoSpark;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import java.util.concurrent.TimeoutException;

public class StreamingApp {
    public static void main(String[] args) throws StreamingQueryException, TimeoutException {
        System.setProperty("hadoop.home.dir", "C:\\bigdata\\hadoop");

        // Kafka broker and topic
        String kafkaBroker = "localhost:9092";
        String topic = "search-analysis-stream";
        
        SparkSession spark = SparkSession.builder()
                .master("local")
                .appName("Spark Search Analysis")
                .config("spark.mongodb.output.uri", "mongodb://127.0.0.1/eticaret.popularproducts")
                .getOrCreate();
        
        StructType schema = new StructType()
                .add("search", DataTypes.StringType)
                .add("region", DataTypes.StringType)
                .add("current_ts", DataTypes.StringType)
                .add("userid", DataTypes.IntegerType);
        Dataset<Row> kafkaDF = spark.readStream().format("kafka")
                .option("kafka.bootstrap.servers", kafkaBroker)
                .option("subscribe", topic)
                .load();
        
        Dataset<Row> extractedDF = kafkaDF.selectExpr("CAST(value AS STRING)")
                .select(functions.from_json(functions.col("value"), schema).as("data"))
                .select("data.*");

        Dataset<Row> maskFilter = extractedDF.filter(
                extractedDF.col("search").equalTo("mask")
        );

        maskFilter.writeStream()
                .trigger(Trigger.ProcessingTime(60000))
                .foreachBatch(new VoidFunction2<Dataset<Row>, Long>() {
                    @Override
                    public void call(Dataset<Row> rowDataset, Long aLong) throws Exception {
                        MongoSpark.write(rowDataset)
                                .option("collection", "searchMask")
                                .mode("append")
                                .save();
                    }
                }).start().awaitTermination();

//        maskFilter.writeStream().format("console").outputMode("append").start().awaitTermination();


    }
}
