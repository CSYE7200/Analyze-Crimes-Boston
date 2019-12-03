import org.apache.spark.sql.Dataset
import org.apache.spark.mllib.regression.LinearRegressionWithSGD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors

object CrimePrediction {
  val DA = new DataAnalysis
  import DA.spark.implicits._

  def main(args: Array[String]): Unit = {
    // Load and parse the data
    val (columns, initDf) = DA.read("src/crime.csv")
    val ds: Dataset[Crimes] = initDf.as[Crimes]
    val rdd = ds.rdd
    //    val (columns, initDf) = DA.read("src/crime.csv")
//    val ds: Dataset[Crimes] = initDf.as[Crimes]
//    val numIterations = 100
//    val model = LinearRegressionWithSGD.train(parsedData, numIterations)
  }
}
