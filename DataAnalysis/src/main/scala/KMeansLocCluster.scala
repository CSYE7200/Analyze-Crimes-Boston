import org.apache.spark.sql.Dataset


/** Describes location in two-dimensional space.
 *
 *  Note: deliberately uses reference equality.
 */
case class Point(val x: Double, val y: Double) {
  private def square(v: Double): Double = v * v
  def squareDistance(that: Point): Double =
    square(that.x - x)  + square(that.y - y)
  private def round(v: Double): Double = (v * 100).toInt / 100.0
  override def toString = s"(${round(x)}, ${round(y)}})"
}

object KMeansLocCluster {

  val DA = new DataAnalysis
  import DA.spark.implicits._

  def main(args: Array[String]): Unit = {
    val (columns, initDf) = DA.read("src/crime.csv")
    val ds: Dataset[Crimes] = initDf.as[Crimes]
    val location = for(p <- ds) yield Point(p.latitude, p.longitude)
    location.filter(p => p.x != -1.0 && p.y != -1.0).show
  }
}
