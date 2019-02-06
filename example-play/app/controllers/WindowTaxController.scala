package controllers

import cats.implicits._
import cats.kernel.Monoid
import javax.inject._
import ltbs.uniform._
import ltbs.uniform.web._
import ltbs.uniform.web.parser._
import ltbs.uniform.interpreters.playframework._
import ltbs.uniform.sampleprograms.WindowTax._
import ltbs.uniform.widgets.govuk._
import org.atnos.eff._
import play.api._
import play.api.i18n.{Messages => _, _}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import play.twirl.api.Html

import InferParser._

@Singleton
class WindowTaxController @Inject()(
  implicit val messagesApi: MessagesApi
) extends Controller with PlayInterpreter with I18nSupport {

  def messages(request: Request[AnyContent]): Messages =
    convertMessages(messagesApi.preferred(request))

  def renderForm(
    key: String,
    errors: ErrorTree,
    tell: Html,
    form: Html,
    breadcrumbs: List[String],
    request: Request[AnyContent],
    messagesIn: Messages
  ): Html = {
    views.html.chrome(key, errors, tell |+| form, breadcrumbs)(messagesIn, request)
  }

  def listingPage[A](
    key: String,
    errors: ErrorTree,
    elements: List[A],
    messages: Messages
  )(implicit evidence$1: Htmlable[A]): Html = ???

  val persistence = new Persistence {
    private var data: DB = Monoid[DB].empty
    def dataGet: Future[DB] = Future.successful(data)
    def dataPut(dataIn: DB): Future[Unit] =
      Future(data = dataIn).map{_ => ()}
  }

  def daylightRobbery(implicit key: String) = {

    type STACKZ = FxAppend[
      Fx.fx3[Uniform[List[Window],ListControl,?], Uniform[Unit,Window,?], UniformAsk[List[Window],?]],
      PlayStack
    ]

    def fu : List[Window] => Html = fu2.toHtml _
    def fu2 : Htmlable[List[Window]] = new Htmlable[List[Window]] {
      def toHtml(lw: List[Window]): Html =
        Html(lw.zipWithIndex.map(_.toString).mkString("<br />"))
    }

    def delistSub[S: _uniform[Unit,Window,?]](
      key: String,
      existing: List[Window],
      default: Option[Window]
    ): Eff[S,Window] = uask[Window,S](s"add")

    Action.async { implicit request =>
      runWeb(
        program = program[STACKZ]
          .delist{
            (key: String, existing: List[Window], default: Option[Window]) =>
            delistSub(key,existing,default)
          }
          .useForm(fu, PlayForm.automatic[ListControl])
          .useForm(PlayForm.automatic[Window]),
        persistence
      )(
        a => Future.successful(Ok(s"You have £$a to pay"))
      )
    }
  }

}