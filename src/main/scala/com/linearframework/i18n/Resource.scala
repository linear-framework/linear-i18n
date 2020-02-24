package com.linearframework.i18n

import java.util.Properties
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

/**
 * A Resource extracted from a ResourceBundle.
 */
class Resource private[i18n](final private val props: Properties) {

  /**
   * Fetches the String with the given key from this resource bundle
   * @param key           the key of the String to fetch
   * @param formatObjects any arguments to pass to String.format() upon returning the result
   */
  def apply(key: String, formatObjects: AnyRef*): Option[String] = {
    val prop = props.getProperty(key)
    if (prop == null) {
      None
    }
    else {
      if (formatObjects.isEmpty) {
        Option(prop)
      }
      else {
        var unwrapped = false
        var unwrappedFormats = formatObjects.toArray
        while (!unwrapped) {
          try {
            unwrappedFormats = unwrappedFormats.head.asInstanceOf[Array[AnyRef]]
          }
          catch {
            case NonFatal(_) => unwrapped = true
          }
        }
        Option(String.format(prop, unwrappedFormats: _*))
      }
    }
  }

  /**
   * Converts this resource to a String map
   */
  def toMap: Map[String, String] = {
    props.asScala.toMap
  }

}
