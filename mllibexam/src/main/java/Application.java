import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.ml.regression.LinearRegressionTrainingSummary;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class Application {
    public static void main(String[] args) {

        SparkSession sparkSession = SparkSession.builder().master("local").appName("spark-mllib").getOrCreate();

        Dataset<Row> dataset = sparkSession.read().format("csv")
                .option("header", true)
                .option("inferSchema", true)
                .load("C:\\Users\\Master\\Desktop\\Big Data\\Datasets\\MLlib\\satis.csv");

//        dataset.show();




        // --Data Preprocessing--

        VectorAssembler vectorAssembler = new VectorAssembler()
                .setInputCols(new String[]{"Ay"})
                .setOutputCol("features");

        Dataset<Row> transformedDS = vectorAssembler.transform(dataset);



        Dataset<Row> finalDS = transformedDS.select("features", "Satis");

        // train-test split
        Dataset<Row>[] splits = finalDS.randomSplit(new double[]{0.7, 0.3},42);

        Dataset<Row> trainData = splits[0];
        Dataset<Row> testData = splits[1];


        //--Create Model--

        LinearRegression lr = new LinearRegression();
        lr.setLabelCol("Satis");


        //--Train Model--
        LinearRegressionModel model = lr.fit(trainData);

        LinearRegressionTrainingSummary summary = model.summary();
        System.out.println("r2 score: " + summary.r2());


        // --Predictions--

        Dataset<Row> predictions = model.transform(testData);
        predictions.show();

        //-- New Data Predictions--

       /* Dataset<Row> newData = sparkSession.read().format("csv")
                .option("header", true)
                .option("inferSchema", true)
                .load("C:\\Users\\Master\\Desktop\\Big Data\\Datasets\\MLlib\\test.csv");

        Dataset<Row> transformedNewData = vectorAssembler.transform(newData);

        Dataset<Row> newPredictions = model.transform(transformedNewData);

        newPredictions.show();*/
    }
}
