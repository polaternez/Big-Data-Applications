import org.apache.spark.ml.classification.NaiveBayes;
import org.apache.spark.ml.classification.NaiveBayesModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class Application {
    public static void main(String[] args) {

        SparkSession sparkSession = SparkSession.builder().master("local").appName("spark-mllib-naive-bayes").getOrCreate();

        Dataset<Row> dataset = sparkSession.read().format("csv")
                .option("header", true)
                .option("inferSchema", true)
                .load("C:\\Users\\Master\\Desktop\\Big Data\\Datasets\\MLlib\\basketbol.csv");

//        dataset.show();

        // --Data Preprocessing--
        // Label Encoding
        StringIndexer weatherIndexer = new StringIndexer().setInputCol("hava").setOutputCol("weather_index");
        StringIndexer heatIndexer = new StringIndexer().setInputCol("sicaklik").setOutputCol("heat_index");
        StringIndexer humidityIndexer = new StringIndexer().setInputCol("nem").setOutputCol("humidity_index");
        StringIndexer windIndexer = new StringIndexer().setInputCol("ruzgar").setOutputCol("wind_index");
        StringIndexer labelIndexer = new StringIndexer().setInputCol("basketbol").setOutputCol("label");

        Dataset<Row> indexedWeather = weatherIndexer.fit(dataset).transform(dataset);
        Dataset<Row> indexedHeat= heatIndexer.fit(indexedWeather).transform(indexedWeather);
        Dataset<Row> indexedHumidity = humidityIndexer.fit(indexedHeat).transform(indexedHeat);
        Dataset<Row> indexedWind = windIndexer.fit(indexedHumidity).transform(indexedHumidity);
        Dataset<Row> indexedResult = labelIndexer.fit(indexedWind).transform(indexedWind);

//        indexedResult.show();

        VectorAssembler vectorAssembler = new VectorAssembler()
                .setInputCols(new String[]{"weather_index", "heat_index", "humidity_index", "wind_index"})
                .setOutputCol("features");

        Dataset<Row> transformedDS = vectorAssembler.transform(indexedResult);

        Dataset<Row> finalDS = transformedDS.select("features", "label");

        // train-test split
        Dataset<Row>[] splits = finalDS.randomSplit(new double[]{0.7, 0.3},42);
        Dataset<Row> trainData = splits[0];
        Dataset<Row> testData = splits[1];


        // --Create Model--
        NaiveBayes nb = new NaiveBayes();
        nb.setSmoothing(1);

        // --Train Model--
        NaiveBayesModel model = nb.fit(trainData);

        // --Predictions--
        Dataset<Row> predictions = model.transform(testData);
        predictions.show();

        // --Evaluate Model--
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                .setLabelCol("label")
                .setPredictionCol("prediction")
                .setMetricName("accuracy");

        double evaluate = evaluator.evaluate(predictions);
        System.out.println("accuracy : " + evaluate);
    }
}
