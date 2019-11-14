import org.apache.spark.sql._
import org.apache.spark.sql.types._

// crimes info
case class Crimes(id: String,
                  offenseCode: Int,
                  offenseCodeGroup: String,
                  offenseDescription: String,
                  year: Int,
                  month: Int,
                  date: Int,
                  hour: Int,
                  dayOfWeek: String,
                  street: String,
                  latitude: Double,
                  longitude: Double)

object DataAnalysis {

  lazy val spark = SparkSession.builder().appName("AnalyzeCrime").master("local[*]").getOrCreate()

  import spark.implicits._

  def main(args: Array[String]): Unit = {
    val (columns, initDf) = read("src/crime.csv")
    val ds: Dataset[Crimes] = initDf.as[Crimes]
    ds.show()
  }

  /** @return The read DataFrame along with its column names. */
  def read(resource: String): (List[String], DataFrame) = {
    val rdd = spark.sparkContext.textFile(resource)

    val headerColumns = rdd.first().split(",").to[List]
    // Compute the schema based on the first line of the CSV file
    val schema = dfSchema(headerColumns)

    val data = rdd.mapPartitionsWithIndex((i, it) => if (i == 0) it.drop(1) else it) // skip the header line
      .map(_.split(",").to[List]).filter(x => x.size == 18).map(row)

    val dataFrame =
      spark.createDataFrame(data, schema)

    (headerColumns, dataFrame)
  }

  /** @return The schema of the DataFrame
   * @param columnNames Column names of the DataFrame
   */
  def dfSchema(columnNames: List[String]): StructType = {

    // the 3rd parameter indicates whether this column can have null value
    val id = StructField("id", StringType, false)
    val offenseCode = StructField("offenseCode", IntegerType, false)
    val offenseCodeGroup = StructField("offenseCodeGroup", StringType, false)
    val offenseDescription = StructField("offenseDescription", StringType, false)
    val year = StructField("year", IntegerType, true)
    val month = StructField("month", IntegerType, false)
    val date = StructField("date", IntegerType, false)
    val hour = StructField("hour", IntegerType, false)
    val dayOfWeek = StructField("dayOfWeek", StringType, false)
    val street = StructField("street", StringType, true)
    val latitude = StructField("latitude", DoubleType, false)
    val longitude = StructField("longitude", DoubleType, false)

    StructType(List(id, offenseCode, offenseCodeGroup, offenseDescription, year, month, date, hour, dayOfWeek, street, latitude, longitude))
  }

  /** @return An RDD Row compatible with the schema produced by `dfSchema`
   * @param line Raw fields
   */
  def row(line: List[String]): Row = {
    val id = line(0)
    val offenseCode = line(1).toInt
    val offenseCodeGroup = line(2)
    val offenseDescription = line(3)
    val year = line(8).toInt
    val month = line(9).toInt
    val date = line(7).split("\\W")(2).toInt
    val hour = line(11).toInt
    val dayOfWeek = line(10)
    val street = line(13)
    val latitude = line(14).toDouble
    val longitude = line(15).toDouble
    Row.fromSeq(List(id, offenseCode, offenseCodeGroup, offenseDescription, year, month, date, hour, dayOfWeek, street, latitude, longitude))
  }
}
