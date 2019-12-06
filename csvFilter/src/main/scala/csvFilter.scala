import java.io.PrintWriter
import java.io.File
import scala.io.Source

class csvFilter(){

}

object csvFilter {

  var A : Map[String, Int] = Map()

  var B : Map[String, Seq[String]] = Map()

  var C : Map[String, Double] = Map()

  def calc(seq1: Seq[String], seq2: Seq[String]): Double = {
    var distance = 0.0
    for (i <-0 to 49) {
      var x1 = seq1.apply(i).toDouble
      var x2 = seq2.apply(i).toDouble
      distance += ((x1 - x2) * (x1 - x2))
    }
    Math.sqrt(distance)
  }

  def main(args : Array[String]): Unit ={
    var t = 0;
    var bufferedSource = Source.fromFile("/Users/saigou/Downloads/glove.6B.50d.txt")
    for (line <- bufferedSource.getLines()) {
      var cols : Seq[String] = line.split(" ");
      t = t + 1;
      A += (cols.head -> 0)
    }
    var bufferedSource2 = Source.fromFile("/Users/saigou/Downloads/data.csv")
    for (line <- bufferedSource2.getLines()) {
      var cols : Seq[String] = line.split(",")
      var cols2 : Seq[String] = cols.tail.head.split(" ")
      for (s <- cols2) {
        if (A.contains(s.toLowerCase)) {
          A += (s.toLowerCase -> 1)
        }
      }
    }
    A = A.filter((t)=> (t._2!=0 || t._1.equals("violence")))

    println(A.keySet.size)

    var bufferedSource3 = Source.fromFile("/Users/saigou/Downloads/glove.6B.50d.txt")

    for (line <- bufferedSource3.getLines()) {
      var cols : Seq[String] = line.split(" ");
      if (A.contains(cols.head)) {
        B += (cols.head -> cols.tail)
      }
    }
    println((B.keySet.size))

    var bufferedSource4 = Source.fromFile("/Users/saigou/Downloads/data.csv")
    /*for (line <- bufferedSource4.getLines()) {
      var cols : Seq[String] = line.split(",")
      var cols2 : Seq[String] = cols.tail.head.split(" ")
      var minf = 1000000.0;
      for (s <- cols2) {
        if (A.contains(s.toLowerCase)) {
          if (minf > calc(B(s.toLowerCase()), B("violent"))) {
            minf = calc(B(s.toLowerCase()), B("violent"))
          }
        }
      }
    }*/
    for (x <- A) {
      C += (x._1 -> calc(B(x._1), B("violence")))
      println(x._1  + "   " + C(x._1))
    }
    val writer = new PrintWriter(new File("/Users/saigou/Downloads/data_r.csv"))
    for (line <- bufferedSource4.getLines()) {
      var cols : Seq[String] = line.split(",")
      var cols2 : Seq[String] = cols.tail.head.split(" ")
      var minf = 1000000.0;
      for (s <- cols2) {
        if (C.contains(s.toLowerCase)) {
          if (minf > C(s.toLowerCase())) {
            minf = C(s.toLowerCase())
          }
        }
      }
      if (minf < 6.1) {
        writer.println(line)
      }
    }
    writer.close()
  }
}
