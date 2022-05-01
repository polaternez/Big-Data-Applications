import org.apache.spark.ml.classification.NaiveBayes;
import org.apache.spark.ml.classification.NaiveBayesModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DiabetesApp {
    public static void main(String[] args) {
        SparkSession sparkSession = SparkSession.builder().master("local").appName("diabetes-mllib").getOrCreate();

        Dataset<Row> dataset = sparkSession.read().format("csv")
                .option("header", true)
                .option("inferSchema", true)
                .load("C:\\Users\\Master\\Desktop\\Big Data\\Datasets\\MLlib\\diabetes.csv");

//        dataset.show();

        // --Data Preprocessing--
        String[] headerList = {"Pregnancies", "Glucose", "BloodPressure", "SkinThickness", "Insulin", "BMI",
                "DiabetesPedigreeFunction", "Age", "Outcome"};

        List<String> headers = Arrays.asList(headerList);
        List<String> headersResult = new ArrayList<String>();

        for (String h : headers){
            if (h.equals("Outcome")){
                StringIndexer tmpIndexer = new StringIndexer().setInputCol(h).setOutputCol("label");
                dataset = tmpIndexer.fit(dataset).transform(dataset);
                headersResult.add("label");

            }
            else {
                StringIndexer tmpIndexer = new StringIndexer().setInputCol(h).setOutputCol(h.toLowerCase() + "_index");
                dataset = tmpIndexer.fit(dataset).transform(dataset);
                headersResult.add(h.toLowerCase() + "_index");
            }
        }
        String[] colList = headersResult.toArray(new String[headersResult.size()]);

        VectorAssembler vectorAssembler = new VectorAssembler().setInputCols(colList).setOutputCol("features");

        Dataset<Row> transformedDS = vectorAssembler.transform(dataset);
        Dataset<Row> finalDS = transformedDS.select("features", "label");

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
