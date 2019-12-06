import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import scala.collection.mutable.HashMap
import scala.collection.{GenMap, GenSeq}
import org.scalactic.TolerantNumerics

case class Point(val x: Double, val y: Double) {
  private def square(v: Double): Double = v * v
  def squareDistance(that: Point): Double =
    square(that.x - x) + square(that.y - y)
}

class KMeansLocClusterSpec extends FunSuite with Matchers with BeforeAndAfter {

  private val master = "local[*]"
  private val appName = "data_process_testing"

  var spark: SparkSession = _

  test("Testing findAverage Method") {
    val km = new KMeans()
    val points1 = Seq(
      Point(1, 0),
      Point(-1, 0)
    )

    val points2 = Seq(
      Point(2, 0),
      Point(0, 0)
    )

    km.findAverage(points1) should equal (Point(0, 0))
    km.findAverage(points2) should equal (Point(1, 0))
  }

  test("Testing findClosest Method") {
    val km = new KMeans()
    val allPoints = Seq(
      Point(1, 0),
      Point(2, 0),
      Point(3, 0)
    )

    km.findClosest(Point(0,0), allPoints) should equal (Point(1, 0))
    km.findClosest(Point(4,0), allPoints) should equal (Point(3, 0))
  }

  test("Testing classify Method") {
    val km = new KMeans()
    val points = Seq(
      Point(0, 4),
      Point(0, 5),
      Point(0, -4),
      Point(0, -5)
    )

    val means = Seq(
      Point(0, 6),
      Point(0, -6)
    )

    val result = km.classify(points, means)
    var e1 = HashMap[Point, Seq[Point]]()
    val s1 = Seq(Point(0, 4), Point(0, 5))
    var e2 = e1 + (Point(0, 6) -> s1 )
    val s2 = Seq(Point(0, -4), Point(0, -5))
    val expectResult = e2 + (Point(0, -6) -> s2)
    result should equal (expectResult)
  }


  test("Testing update Method") {
    val km = new KMeans()

    val oldMeans = Seq(
      Point(0, 6),
      Point(0, -6)
    )

    var e1 = HashMap[Point, Seq[Point]]()
    val s1 = Seq(Point(0, 4), Point(0, 5))
    var e2 = e1 + (Point(0, 6) -> s1 )
    val s2 = Seq(Point(0, -4), Point(0, -5))
    val testMap = e2 + (Point(0, -6) -> s2)

    val actualRes = km.update(testMap, oldMeans)

    val expectedRes = Seq(Point(0.0, 4.5), Point(0.0, -4.5))
    actualRes should equal (expectedRes)
  }


  implicit val doubleEquality = TolerantNumerics.tolerantDoubleEquality(0.005)
  /* This test case is to test whether satisfy appceptance criteria */
  test("Testing SSE value (should less than 0.001)") {

    Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)

    val DA = new DataAnalysis
    import DA.spark.implicits._
    val (columns, initDf) = DA.read("static/finalCSV.csv")
    val ds: Dataset[Crimes] = initDf.as[Crimes]
    val kMeans = new KMeans
    val loc = ds.filter(x => x.latitude != -1.0 && x.longitude != -1.0) // filter incorrect values

    val location = for (p <- loc) yield Point(p.latitude, p.longitude)
    val initialPoints = Seq(
      Point(42.337170, -71.075436),
      Point(42.358284, -71.060151),
      Point(42.359682, -71.125615),
      Point(42.372825, -71.065957),
      Point(42.368745, -71.040402),
      Point(42.327528, -71.057069),
      Point(42.313024, -71.077571),
      Point(42.314067, -71.102221),
      Point(42.303542, -71.065528),
      Point(42.343135, -71.041523))

    location.persist()
    val resultPoints = kMeans.kMeans(location.collect(), initialPoints, 50, 0)

    val cluster = kMeans.classify(location.collect(), resultPoints)

    val SSE = cluster
      .map(p => p._2.map(q => Math.pow(q.x - p._1.x, 2) + Math.pow(q.y - p._1.y, 2)))
      .map(r => r.reduce(_+_)/r.length)

    for(sse <- SSE) sse should be < 0.001
  }
}