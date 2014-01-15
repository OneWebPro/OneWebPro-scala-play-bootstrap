package pl.onewebpro.flash

import play.api.mvc.{RequestHeader, Flash}
import scalaz.Value

/**
 * @author loki
 */

case object FlashType extends Enumeration {
  type FlashType = Value

  val Danger = Value("danger")
  val Warning = Value("warning")
  val Info = Value("info")
  val Success = Value("success")
  val None = Value
}

trait FlashInterface {
  /**
   * Method for displaying danger message used with bootstrap.messages view.
   * @param value String
   * @return Flash
   */
  def danger(value: String): Flash

  /**
   * Method for displaying warning used with bootstrap.messages view.
   * @param value String
   * @return Flash
   */
  def warning(value: String): Flash

  /**
   * Method for displaying information message used with bootstrap.messages view.
   * @param value String
   * @return Flash
   */
  def info(value: String): Flash

  /**
   * Method for displaying success message used with bootstrap.messages view.
   * @param value String
   * @return Flash
   */
  def success(value: String): Flash

  /**
   * Method returns all keys and values from flash
   * @return
   */
  def all: Map[String, String]

  /**
   * Method return which type of key is this key
   * @param value String
   * @return FlashType.Value
   */
  def getType(value: String): FlashType.Value
}

case class WrapperFlash(f: Flash) extends FlashInterface {

  /**
   * Method for displaying danger message used with bootstrap.messages view.
   * @param value String
   * @return Flash
   */
  def danger(value: String): Flash = {
    f.+(FlashWrapper.fValue(FlashType.Danger), value)
  }

  /**
   * Method for displaying warning used with bootstrap.messages view.
   * @param value String
   * @return Flash
   */
  def warning(value: String): Flash = {
    f.+(FlashWrapper.fValue(FlashType.Warning), value)
  }

  /**
   * Method for displaying information message used with bootstrap.messages view.
   * @param value String
   * @return Flash
   */
  def info(value: String): Flash = {
    f.+(FlashWrapper.fValue(FlashType.Info), value)
  }

  /**
   * Method for displaying success message used with bootstrap.messages view.
   * @param value String
   * @return Flash
   */
  def success(value: String): Flash = {
    f.+(FlashWrapper.fValue(FlashType.Success), value)
  }

  /**
   * Method returns all keys and values from flash
   * @return
   */
  def all: Map[String, String] = {
    f.data
  }

  /**
   * Method return which type of key is this key
   * @param value String
   * @return FlashType.Value
   */
  def getType(value: String): FlashType.Value = FlashWrapper.fType(value)

  /**
   * Check if type is NONE
   * @param value String
   * @return
   */
  def isNone(value: String): Boolean = {
    getType(value) match {
      case FlashType.None => true
      case _ => false
    }
  }

}

case object FlashWrapper {

  implicit def session(wf: WrapperFlash): Flash = wf.f

  implicit def wf(f: Flash): WrapperFlash = WrapperFlash(f)

  implicit def rw(request: RequestHeader): WrapperFlash = WrapperFlash(request.flash)

  implicit def value(f: Value): FlashType.Value = f.asInstanceOf[FlashType.Value]

  implicit def fValue(f: FlashType.Value): String = f.toString

  implicit def fType(f: String): FlashType.Value = {
    f match {
      case "danger" => FlashType.Danger
      case "warning" => FlashType.Warning
      case "info" => FlashType.Info
      case "success" => FlashType.Success
      case _ => FlashType.None
    }
  }
}

case object FlashMessage {

  import FlashWrapper._

  /**
   * Method for displaying danger message used with bootstrap.messages view.
   * @param value String
   * @return Flash
   */
  def danger(value: String)(implicit request: RequestHeader): Flash = request.danger(value)

  /**
   * Method for displaying warning used with bootstrap.messages view.
   * @param value String
   * @return Flash
   */
  def warning(value: String)(implicit request: RequestHeader): Flash = request.warning(value)

  /**
   * Method for displaying information message used with bootstrap.messages view.
   * @param value String
   * @return Flash
   */
  def info(value: String)(implicit request: RequestHeader): Flash = request.info(value)

  /**
   * Method for displaying success message used with bootstrap.messages view.
   * @param value String
   * @return Flash
   */
  def success(value: String)(implicit request: RequestHeader): Flash = request.success(value)
}

