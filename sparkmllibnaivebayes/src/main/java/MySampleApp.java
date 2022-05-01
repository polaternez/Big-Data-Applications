import org.apache.spark.ml.classification.NaiveBayes;
import org.apache.spark.ml.classification.NaiveBayesModel;
import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.StandardScaler;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MySampleApp {
    public static void main(String[] args) {

        SparkSession sparkSession = SparkSession.builder().master("local").appName("diabetes-mllib").getOrCreate();

        Dataset<Row> dataset = sparkSession.read()
                .option("header", true)
                .option("inferSchema", true)
                .csv("C:\\Users\\Master\\Desktop\\Big Data\\Datasets\\MLlib\\diabetes.csv");

//        dataset.show();

        // --Data Preprocessing--
        
        String[] headers = {"Pregnancies", "Glucose", "BloodPressure", "SkinThickness", "Insulin", "BMI",
                "DiabetesPedigreeFunction", "Age"};

        VectorAssembler vectorAssembler = new VectorAssembler().setInputCols(headers).setOutputCol("features");
        
        Dataset<Row> transformDS = vectorAssembler.transform(dataset);

        // Scaling
        StandardScaler scaler = new StandardScaler()
                .setInputCol("features")
                .setOutputCol("scaledFeatures")
                .setWithStd(true)
                .setWithMean(false);

        Dataset<Row> scaledDS = scaler.fit(transformDS).transform(transformDS);

        Dataset<Row> finalDS = scaledDS.select("scaledFeatures", "Outcome")
                .withColumnRenamed("scaledFeatures", "features");

        // train-test split
        Dataset<Row>[] splits = finalDS.randomSplit(new double[]{0.7, 0.3},42);
        Dataset<Row> trainData = splits[0];
        Dataset<Row> testData = splits[1];

        // --Create Model--
        RandomForestClassifier rfc = new RandomForestClassifier()
                .setLabelCol("Outcome")
                .setFeaturesCol("features");

        // --Train Model--
        RandomForestClassificationModel model = rfc.fit(trainData);

        // --Predictions--
        Dataset<Row> predictions = model.transform(testData);
        predictions.show();

        // --Evaluate Model--
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                .setLabelCol("Outcome")
                .setPredictionCol("prediction")
                .setMetricName("accuracy");

        double evaluate = evaluator.evaluate(predictions);
        System.out.println("accuracy : " + evaluate);
    }
}
