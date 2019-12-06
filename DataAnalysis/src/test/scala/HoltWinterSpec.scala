import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.linalg.DenseVector
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import org.apache.spark.sql._

class HoltWinterSpec extends FunSuite with Matchers with BeforeAndAfter {
  Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
  Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)
  private val master = "local[*]"
  private val appName = "data_process_testing"

  var spark: SparkSession = _


  test("Load dataframe as same as expected dataframe") {
    spark = new SparkSession.Builder().appName(appName).master(master).getOrCreate()
    val sQLContext = spark.sqlContext
    import sQLContext.implicits._

    val DA = new DataAnalysis
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

    model.alpha should be (0.0 +- 0.01)
    model.beta should be (0.01 +- 0.01)
    model.gamma should be (0.5 +- 0.1)
  }

}