import org.apache.log4j.{Level, Logger}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import org.apache.spark.sql._
import org.apache.spark.sql.types._

class DataProcessSpec extends FunSuite with Matchers with BeforeAndAfter {
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
    val (columns, actualDF) = DA.read("static/testDataProcess.csv")

    val expectedSchema = List(
    StructField("id", StringType, nullable = false),
    StructField("offenseCode", IntegerType, nullable = false),
    StructField("offenseCodeGroup", StringType, nullable = false),
    StructField("offenseDescription", StringType, nullable = false),
    StructField("year", IntegerType, nullable = true),
    StructField("month", IntegerType, nullable = false),
    StructField("date", IntegerType, nullable = false),
    StructField("hour", IntegerType, nullable = false),
    StructField("dayOfWeek", StringType, nullable = false),
    StructField("street", StringType, nullable = true),
    StructField("latitude", DoubleType, nullable = false),
    StructField("longitude", DoubleType, nullable = false)
    )

    val expectedData = Seq(
      Row("I182070943", 1402, "Vandalism", "VANDALISM", 2018, 8, 21, 0, "Tuesday", "HECLA ST", 42.30682138, -71.06030035)
    )

    val expectedDF = spark.createDataFrame(
      spark.sparkContext.parallelize(expectedData),
      StructType(expectedSchema)
    )

    expectedDF.count() should equal (actualDF.count())
    expectedDF.except(actualDF).count() should equal(0)
  }

}