//import org.scalatest.tagobjects.Slow
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import org.apache.spark.sql._
import org.apache.spark.sql.types._

class DataProcessSpec extends FunSuite with Matchers with BeforeAndAfter {

  private val master = "local[*]"
  private val appName = "data_process_testing"

  var spark: SparkSession = _


  test("Load dataframe as same as expected dataframe") {
    spark = new SparkSession.Builder().appName(appName).master(master).getOrCreate()
    val sQLContext = spark.sqlContext
    import sQLContext.implicits._

    val DA = new DataAnalysis
    val (columns, actualDF) = DA.read("static/testDA.csv")

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
      Row("I182070945", 619, "Larceny", "LARCENY ALL OTHERS", 2018, 9, 2, 13, "Sunday", "LINCOLN ST", 42.35779134, -71.13937053)
    )

    val expectedDF = spark.createDataFrame(
      spark.sparkContext.parallelize(expectedData),
      StructType(expectedSchema)
    )

    assert(expectedDF.count().equals(actualDF.count()))
    assert(expectedDF.except(actualDF).isEmpty)
  }

}