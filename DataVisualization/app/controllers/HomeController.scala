package controllers

import javax.inject._
import models.BasicForm
import play.api.mvc._

import scala.io.Source

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with play.api.i18n.I18nSupport{

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def CrimeTypes() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.CrimeTypes())
  }

  def CrimeByMonth() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.CrimeByMonth())
  }

  def CrimeByDay() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.CrimeByDay())
  }

  def CrimeLocation() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.CrimeLocation())
  }

  def CrimeByHour() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.CrimeByHour())
  }

  def CrimePrediction() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.CrimePrediction())
  }

  def TextRank() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.basicForm(BasicForm.form))
  }

  def simpleFormPost() = Action { implicit request: Request[AnyContent] =>
    val formData: BasicForm = BasicForm.form.bindFromRequest.get // Careful: BasicForm.form.bindFromRequest returns an Option
    //Ok(views.html.result(formData.month, formData.day, formData.year, formData.street, formData.ty)) // just returning the data because it's an example :
    Ok(views.html.TextRankResult(getSearchResult(formData)))
  }

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  def minEditDistance(s1: String, s2: String): Int = {
    val mat = Array.ofDim[Int](s2.length+1, s1.length+1)
    mat(0)(0) = 0
    for (i <- 1 to s1.length) {
      mat(0)(i) = i
    }
    for (i <- 1 to s2.length) {
      mat(i)(0) = i
    }
    for(i <- 1 to s2.length) {
      for(j <- 1 to s1.length) {
        var tmp = mat(i-1)(j) + 1;
        if (mat(i)(j-1) + 1 < tmp) { tmp = mat(i)(j-1) + 1}
        var tmp2 = mat(i-1)(j-1)
        if (s1.charAt(j-1) != s2.charAt(i-1)) {
          tmp2 = tmp2 + 1;
        }
        if (tmp2 < tmp) {tmp = tmp2}
        mat(i)(j) = tmp;
      }
    }
    return mat(s2.length)(s1.length);
  }

  def judgeString(s1: String, s2: String): Int = {
    if (s2.equals("")) return 0;
    var sq_s1: Seq[String] = s1.split(" ")
    var sq_s2: Seq[String] = s2.split(" ")
    var score_f = 0;
    for (x <- sq_s2) {
      var minf = 10000;
      for (y <- sq_s1) {
        var tmp = minEditDistance(x,y);
        if (tmp < minf) {
          minf = tmp;
        }
      }
      score_f = score_f + minf;
    }
    println("-------     " + score_f)
    return score_f
  }

  def judgeNumber(s1: String, n1 : Int, n2: Int, n3: Int): Int = {
    var tmp_s : String = ""
    val s_data : Seq[String] = s1.split(" ")
    val sq_data : Seq[String] = s_data.head.split("/")
    val x3 = n3 - 2000
    println(s1)
    println(n1 + " " + n2 + " " + x3)
    println(sq_data)

    if (n1 != 0 && n1 != sq_data.head.toInt) return 1000;
    if (n2 != 0 && n2 != sq_data.tail.head.toInt) return 1000;
    if (n3 != 0 && x3 != sq_data.tail.tail.head.toInt) return 1000;
    println("in this place")
    return 0;
  }

  def getMax(l : List[Int]): Int = {
    var loc = 0;
    var maxf = l.apply(0);
    for (i <- 0 to l.length-1) {
      if (l.apply(i) > maxf) {
        maxf = l.apply(i);
        loc = i;
      }
    }
    loc;
  }

  def getSearchResult(formdata : BasicForm): List[List[String]] ={
    var bufferedSource = Source.fromFile("/Users/steve/IDEAProjects/CSYE7200_FinalProject/DataVisualization/app/controllers/data_r.csv")
    // var bufferedSource = Source.fromFile("/Users/saigou/Downloads/data.csv")
    var resultList: List[List[String]] = List()
    var resultScoreList : List[Int] = List()
    var t = 0
    for (line <- bufferedSource.getLines()) {
      t = t + 1;
      val cols: Seq[String] = line.split(",");
      //println("!!!!    " + cols);
      if (!cols.head.equals("INCIDENT_NUMBER")) {
        val num1 = judgeString(cols.tail.head, formdata.ty);
        val num2 = judgeString(cols.tail.tail.tail.head, formdata.street)
        val num3 = judgeNumber(cols.tail.tail.head, formdata.month, formdata.day, formdata.year)
        val count_num = num1 + num2 + num3;
        if (resultList.length < 10) {
          if (count_num < 5) {
            resultList = resultList :+ cols.toList
            resultScoreList = resultScoreList :+ count_num
          }
        } else {
          val local = getMax(resultScoreList);
          if (resultScoreList.apply(local) > count_num) {
            //println("&&&&&& " + resultList.take(local) + "    "  + resultList.drop(local+1))
            //println("&&&&&& " + resultScoreList.take(local) + "    "  + resultScoreList.drop(local+1))
            resultList = resultList.take(local) ::: resultList.drop(local+1)
            resultScoreList = resultScoreList.take(local) ::: resultScoreList.drop(local+1)
            resultList = resultList :+ cols.toList
            resultScoreList = resultScoreList :+ count_num
          }
        }
        //println("in this")
      } else {
        //println("in that")
      }
      println(resultList.length + "     " + t);
      // if (t == 30) return resultList;
    }
    return resultList;
  }

}
