import org.apache.spark.sql.Dataset
import org.apache.spark.mllib.linalg.{DenseVector, Vectors}

object CrimePrediction {
  val DA = new DataAnalysis
  import DA.spark.implicits._

  def main(args: Array[String]): Unit = {

    // Load and parse the data
    val (columns, initDf) = DA.read("src/crime.csv")
    val ds: Dataset[Crimes] = initDf.as[Crimes]

    val s = ds.select($"year", $"month", $"date")
      .groupBy($"year", $"month",$"date")
      .count
      .orderBy($"year", $"month",$"date")
    s.take(100).foreach(println)
    val getCrimes = s.rdd.map(x => x.getLong(3).toDouble).collect()
    val crimesCount = new DenseVector(getCrimes)
    val period = 30

    val model = HoltWinters.fitModel(crimesCount, period, "additive", "BOBYQA")


    val forecast = new DenseVector(new Array[Double](period))
    model.forecast(crimesCount, forecast)

    for(i <- 0 until period)
      println(forecast(i))
  }
}
