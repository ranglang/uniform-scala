package controllers


import cats.implicits._
import cats.kernel.Monoid
import javax.inject._
import ltbs.uniform._
import ltbs.uniform.web._
import ltbs.uniform.web.parser._
import ltbs.uniform.interpreters.playframework._
import ltbs.uniform.sampleprograms.BeardTax._
import ltbs.uniform.widgets.govuk._
import org.atnos.eff._
import play.api._
import play.api.i18n.{Messages => _, _}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import play.twirl.api.{Html, HtmlFormat}

import InferParser._

@Singleton
class BeardController @Inject()(implicit val messagesApi: MessagesApi) extends Controller with PlayInterpreter with I18nSupport {

  def messages(request: Request[AnyContent]): UniformMessages[Html] = (
    convertMessages(messagesApi.preferred(request)) |+|
      UniformMessages.bestGuess.map(HtmlFormat.escape)
  )

  def renderForm(
    key: List[String],
    errors: ErrorTree,
    form: Html,
    breadcrumbs: List[String],
    request: Request[AnyContent],
    messagesIn: UniformMessages[Html]
  ): Html = {
    views.html.chrome(key.last, errors, form, breadcrumbs)(messagesIn, request)
  }

  val persistence = new Persistence {
    private var data: DB = Monoid[DB].empty
    def dataGet: Future[DB] = Future.successful(data)
    def dataPut(dataIn: DB): Future[Unit] =
      Future(data = dataIn).map{_ => ()}
  }

  def beardAction(key: String) = {
    implicit val keys: List[String] = key.split("/").toList
    Action.async { implicit request =>
      runWeb(
        program = program[FxAppend[TestProgramStack, PlayStack]]
          .useForm(automatic[Unit, Option[MemberOfPublic]])
          .useForm(automatic[Unit, BeardStyle])
          .useForm(automatic[Unit, BeardLength]),
        persistence
      )(
        a => Future.successful(Ok(s"You have £$a to pay"))
      )
    }
  }

}
