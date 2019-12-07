import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.Dataset

import scala.annotation.tailrec
import scala.collection.{GenMap, GenSeq}


/** Describes location in two-dimensional space.
 *
 * Note: deliberately uses reference equality.
 */
case class Point(val x: Double, val y: Double) {
  private def square(v: Double): Double = v * v
  def squareDistance(that: Point): Double =
    square(that.x - x) + square(that.y - y)
}

class KMeans {
  /**
   * Find the closest mean value of each point
   */
  def findClosest(p: Point, means: GenSeq[Point]): Point = {
    var minDistance = p.squareDistance(means(0))
    var closest = means(0)
    var i = 1
    while (i < means.length) {
      val distance = p.squareDistance(means(i))
      if (distance < minDistance) {
        minDistance = distance
        closest = means(i)
      }
      i += 1
    }
    closest
  }

  /**
   * This method takes a generic sequence of points and a generic sequence of means.
   * It returns a generic map collection, which maps each mean to the sequence of
   * points in the corresponding cluster.
   */
  def classify(points: GenSeq[Point], means: GenSeq[Point]): GenMap[Point, GenSeq[Point]] = {
    val groups = points.groupBy { x => findClosest(x, means) }
    means.foldLeft(groups)((groups: GenMap[Point, GenSeq[Point]], x: Point) =>
      if (groups.contains(x)) groups
      else groups ++ GenMap[Point, GenSeq[Point]]((x, GenSeq())))
  }

  /**
   * find the average mean point of given points sequence
   */
  def findAverage(points: GenSeq[Point]): Point = {
      var x = 0.0
      var y = 0.0
      points.foreach { p =>
        x += p.x
        y += p.y
      }
      Point(x / points.length, y / points.length)
    }

  /**
   * update the new means points according to the oldmeans and classfied old means points
   */
  def update(classified: GenMap[Point, GenSeq[Point]], oldMeans: GenSeq[Point]): GenSeq[Point] = {
    for (oldMean <- oldMeans) yield findAverage(classified.get(oldMean).get)
  }

  /**
   * takes a sequence of old means and the sequence of updated means,
   * and returns a boolean indicating if the algorithm converged or not
   */
  def converged(eta: Double)(oldMeans: GenSeq[Point], newMeans: GenSeq[Point]): Boolean = {
    for (i <- 0 until oldMeans.size) {
      if (Math.abs(oldMeans(i).squareDistance(newMeans(i))) > eta)
        return false
    }
    true
  }


  /**
   * KMeans algorithm in a tail recursive way
   * 1. get the classification of all means points and their corresponding data points
   * 2. get the updated new means sequence
   * 3. judge whether old means points and new means points satisfy converged threshold
   * 4. do the tail recursive and return the new means result
   */
  @tailrec
  final def kMeans(points: GenSeq[Point], means: GenSeq[Point], maxIter:Int, eta: Double): GenSeq[Point] = {
    val classification = classify(points, means)
    val newMeans = update(classification, means)
    if (!converged(eta)(means, newMeans) && maxIter >= 0) { // not converged yet and the iteration does not count to 0
      println(maxIter)
      kMeans(points, newMeans, maxIter - 1, eta)
    } else newMeans
  }

}

object KMeansLocCluster {

  val DA = new DataAnalysis

  import DA.spark.implicits._

  def main(args: Array[String]): Unit = {
    // ignore unnecessary log
    Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)

    val (columns, initDf) = DA.read("src/finalCSV.csv")
    val ds: Dataset[Crimes] = initDf.as[Crimes]
    val kMeans = new KMeans
    val loc = ds.filter(x => x.latitude != -1.0 && x.longitude != -1.0) // filter incorrect values
    /**
     * get the latitude and longitude info from dataset
     * initialize 10 cluster centers (Randomly choose 10 from google map)
     */
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
    resultPoints.foreach(println)

  val cluster = kMeans.classify(location.collect(), resultPoints)
    cluster.map(x => x._2.size).foreach(println)


    val SSE = cluster
    .map(p => p._2.map(q => Math.pow(q.x - p._1.x, 2) + Math.pow(q.y - p._1.y, 2)))
    .map(r => r.reduce(_+_)).sum

println(SSE)
  }
}
