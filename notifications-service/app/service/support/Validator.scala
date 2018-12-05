package service.support

import org.apache.commons.lang3.StringUtils
import play.api.libs.json.{Json, Writes}

/**
 * Validation functions
 *
 * @author jaharzli
 */
object Validator {

  case class ValidationError(msg: String)

  object ValidationError {
    implicit val validationErrorWrites: Writes[ValidationError] =
      Json.writes[ValidationError]
  }

  def notNull[A](entity: A, msg: String): Seq[ValidationError] =
    Option(entity) match {
      case Some(x) => Seq.empty[ValidationError]
      case None    => Seq(ValidationError(msg))
    }

  def notBlank(text: String, msg: String): Seq[ValidationError] =
    Option(text)
      .filter(StringUtils.isNotBlank) match {
      case Some(x) => Seq.empty[ValidationError]
      case None    => Seq(ValidationError(msg))
    }

}
