package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

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
    Ok(views.html.TextRank())
  }

  def TextRankResult() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.TextRankResult(List(
      List("I182070945", "Larceny", "9/2/18 13:00", "LINCOLN ST", "42.358929", "-71.058823"),
      List("I182070945", "Larceny", "9/2/18 13:00", "LINCOLN ST", "42.36779134", "-71.12937053"),
      List("I182070945", "Larceny", "9/2/18 13:00", "LINCOLN ST", "42.34979134", "-71.10737053")
    )))
  }
}
