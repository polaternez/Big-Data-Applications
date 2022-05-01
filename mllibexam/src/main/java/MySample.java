import org.apache.spark.ml.feature.Imputer;
import org.apache.spark.ml.feature.ImputerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class MySample {
    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "C:\\hadoop");

        SparkSession sparkSession = SparkSession.builder().master("local").appName("spark-mllib").getOrCreate();

        StructType schema = new StructType()
                .add("ulke", DataTypes.StringType)
                .add("boy", DataTypes.DoubleType)
                .add("kilo", DataTypes.DoubleType)
                .add("yas", DataTypes.DoubleType)
                .add("cinsiyet", DataTypes.StringType);

        Dataset<Row> dataset = sparkSession.read()
                .option("header", true)
                .schema(schema)
                .csv("C:\\Users\\Master\\Desktop\\Big Data\\Datasets\\MLlib\\eksikveriler.csv");

//        dataset.show();
//        dataset.printSchema();

       /* Dataset<Row> newDS = dataset.withColumn("yas_int", dataset.col("yas").cast("int"));
        newDS.printSchema();*/

        Imputer imputer = new Imputer()
                .setInputCols(new String[]{"yas"})
                .setOutputCols(new String[]{"yas_imputed"})
                .setStrategy("mean");

        imputer.fit(dataset).transform(dataset).show();
    }
}
