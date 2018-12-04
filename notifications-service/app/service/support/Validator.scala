package service.support

import cats.data._
import cats.syntax.validated._
import org.apache.commons.lang3.StringUtils

/**
 * Validation functions
 *
 * @author jaharzli
 */
object Validator {

  type ValidationResult[A] = ValidatedNec[String, A]

  def notNull[A](entity: A, msg: String): ValidationResult[A] =
    Option(entity) match {
      case Some(x) => entity.validNec
      case None    => msg.invalidNec
    }

  def notBlank(text: String, msg: String): ValidationResult[String] =
    Option(text)
      .filter(StringUtils.isNotBlank) match {
      case Some(x) => text.validNec
      case None    => msg.invalidNec
    }

}
