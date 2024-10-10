import com.mongodb.spark.MongoSpark;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataType$;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class Application {
    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir","C:\\bigdata\\hadoop");

        // Kafka broker and topic
        String kafkaBroker = "localhost:9092";
        String topic = "search-analysis-stream";

        SparkSession sparkSession = SparkSession.builder()
                .master("local")
                .appName("Spark Search Analysis")
                .config("spark.mongodb.output.uri", "mongodb://127.0.0.1/eticaret.popularproducts")
                .getOrCreate();
        
        StructType schema = new StructType()
                .add("search", DataTypes.StringType)
                .add("region", DataTypes.StringType)
                .add("current_ts", DataTypes.StringType)
                .add("userid", DataTypes.IntegerType);
        
        Dataset<Row> kafkaDF = sparkSession.read().format("kafka")
                .option("kafka.bootstrap.servers", kafkaBroker)
                .option("subscribe", topic)
                .load();

//        kafkaDF.printSchema();

        Dataset<Row> extractedDF = kafkaDF.selectExpr("CAST(value AS STRING)")
                .select(functions.from_json(functions.col("value"), schema).as("jsontostructs"))
                .select("jsontostructs.*");

        // Popular Products - Top 10
        /*Dataset<Row> topTenDF = extractedDF.groupBy("search").count()
                .sort(functions.desc("count")).limit(10);
        topTenDF.show();

        MongoSpark.write(topTenDF).mode("overwrite").save();*/

        // Users Product Selections
        /*Dataset<Row> countDF = extractedDF.groupBy("userid", "search").count()
                .filter("count > 5");
        Dataset<Row> pivot = countDF.groupBy("userid").pivot("search").count()
                .na().fill(0);

        MongoSpark.write(pivot)
                .option("collection", "searchByUserid")
                .mode("overwrite")
                .save();*/

        // Most popular products in different time windows
        Dataset<Row> windowDF = extractedDF.groupBy(
                functions.window(extractedDF.col("current_ts"), "30 minute"),
                extractedDF.col("search")
        ).count();

        MongoSpark.write(windowDF)
                .option("collection", "timeWindowSearch")
                .mode("overwrite")
                .save();
    }
}
