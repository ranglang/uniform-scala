package ltbs.uniform.datapipeline

import play.twirl.api.Html
import shapeless._
import shapeless.labelled._
import cats.implicits._
import cats.Monoid

trait HtmlForm[A]{
  def render(key: String, values: Input, errors: Error, messages: Messages): Html
}

object InferForm {

  implicit val htmlMonoidInstance = new Monoid[Html] {
    def empty: Html = Html("")
    def combine(a: Html, b: Html):Html = Html(a.toString ++ b.toString)
  }

  implicit val hnilForm = new HtmlForm[HNil] {
    def render(key: String, values: Input, errors: Error, messages: Messages): Html =
      Monoid[Html].empty
  }

  implicit def hConsForm[K <: Symbol, H, T <: HList](
    implicit
      witness: Witness.Aux[K],
    hForm: Lazy[HtmlForm[H]],
    tForm: HtmlForm[T]
  ): HtmlForm[FieldType[K,H] :: T] = new HtmlForm[FieldType[K,H] :: T] {
    val fieldName: String = witness.value.name

    def render(key: String, values: Input, errors: Error, messages: Messages): Html =
      hForm.value.render(s"${key}.${fieldName}", values, errors, messages) |+|
        tForm.render(key, values, errors, messages)
  }

  implicit def genericForm[A, H, T]
    (implicit
       generic: LabelledGeneric.Aux[A,T],
     hGenParser: Lazy[HtmlForm[T]]
    ): HtmlForm[A] = new HtmlForm[A]
  {

    def render(key: String, values: Input, errors: Error, messages: Messages): Html =
      hGenParser.value.render(key, values, errors, messages)
  }


}
