import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import scala.collection.mutable.HashMap
import scala.collection.{GenMap, GenSeq}

case class Point(val x: Double, val y: Double) {
  private def square(v: Double): Double = v * v
  def squareDistance(that: Point): Double =
    square(that.x - x) + square(that.y - y)
}

class KMeansLocClusterSpec extends FunSuite with Matchers with BeforeAndAfter {
//  Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
//  Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)

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

    assert(km.findAverage(points1) === Point(0, 0))
    assert(km.findAverage(points2) === Point(1, 0))
  }

  test("Testing findClosest Method") {
    val km = new KMeans()
    val allPoints = Seq(
      Point(1, 0),
      Point(2, 0),
      Point(3, 0)
    )

    assert(km.findClosest(Point(0,0), allPoints) === Point(1, 0))
    assert(km.findClosest(Point(4,0), allPoints) === Point(3, 0))
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
    assert(result.equals(expectResult))
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
    assert(actualRes.equals(expectedRes))
  }
}