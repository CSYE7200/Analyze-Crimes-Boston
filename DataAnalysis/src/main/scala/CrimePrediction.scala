import org.apache.spark.sql.Dataset
import org.apache.spark.mllib.linalg.DenseVector

object CrimePrediction {
  val DA = new DataAnalysis
  import DA.spark.implicits._

  def main(args: Array[String]): Unit = {

    // Load and parse the data
    val (columns, initDf) = DA.read("src/finalCSV.csv")
    val ds: Dataset[Crimes] = initDf.as[Crimes]

    val s = ds.select($"year", $"month", $"date")
      .groupBy($"year", $"month",$"date")
      .count
      .orderBy($"year", $"month",$"date")
    val getCrimes = s.rdd.map(x => x.getLong(3).toDouble).collect()
    val crimesCount = new DenseVector(getCrimes)
    val period = 365

    val model = HoltWinters.fitModel(crimesCount, period, "additive", "BOBYQA")
  print(model.alpha,model.beta, model.gamma)

    val forecast = new DenseVector(new Array[Double](365))
    model.forecast(crimesCount, forecast)

    for(i <- 0 until 365)
      println(forecast(i).toInt)
  }
}
