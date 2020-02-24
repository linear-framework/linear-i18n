package com.linearframework.i18n

import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util
import java.util.{Locale, Properties}
import scala.jdk.CollectionConverters._
import scala.util.Try
import scala.util.control.NonFatal

/**
 * Convenience methods for building [[com.linearframework.i18n.ResourceBundle]] objects.
 */
object ResourceBundle {

  /**
   * Creates a resource bundle using properties files.
   * @param bundleName  the base name of the bundle files
   * @param fallback    (optional) the default (fallback) locale to use when a request comes in for an
   *                    unsupported locale.  If not specified, `Locale.US` is assumed.  This represents the locale
   *                    of the base properties file (i.e., the file without any locale-related suffix in its filename).
   * @param cached (optional) whether or not to cache the bundle in memory or re-load from disk
   *                    on every request.  If not specified, caching is enabled.
   * @param getFileUrl  (optional) the method by which properties files are loaded.  If not specified,
   *                    files are fetched from the classpath.
   */
  def of(
    bundleName: String,
    fallback: Locale = Locale.US,
    cached: Boolean = true,
    getFileUrl: String => URL = ResourceUtils.getClasspathResource
  ): ResourceBundle = {
    new ResourceBundle {
      override protected val bundle: String = bundleName
      override protected val enableCache: Boolean = cached
      override protected val defaultLocale: Locale = fallback
      override protected def fetchFile(fileName: String): URL = getFileUrl(fileName)
    }
  }

}

/**
 * A resource bundle represents a group of properties representing the same values for different locales.
 */
trait ResourceBundle {

  /** The base name of the bundle files */
  protected val bundle: String

  /** The default (fallback) locale to use when no locales match a request */
  protected val defaultLocale: Locale

  /** Whether or not to cache the contents of the bundle in memory */
  protected val enableCache: Boolean

  /**
   * Defines how to fetch the given file.
   * The default behavior is to fetch the given filename from the classpath.
   * @param fileName the name of the properties file to fetch
   */
  protected def fetchFile(fileName: String): URL

  private lazy val cache: util.Map[Locale, (Locale, Properties)] = new util.HashMap[Locale, (Locale, Properties)]()

  /**
   * Gets the resource from this bundle associated with the given locale range.
   * If none of the locales in the given range are supported,
   * or if no locale range was provided, return the resource
   * for the default locale.
   * @param localeRange a q-factor weighted locale range (e.g., the contents of the 'Accept-Language' HTTP header)
   */
  final def forLocale(localeRange: String): Resource = {
    val locales = rangeToLocales(localeRange)
    val properties = locales.map(loadProperties).filterNot { case (_, _, _, props) => props.isEmpty }
    val sortedProperties = properties.zipWithIndex.sortBy { case ((_, priority, _, _), preferredOrder) => (priority, preferredOrder) }
    val preferredProperties = sortedProperties.map { case ((_, priority, parent, props), _) => (priority, parent, props) }.headOption

    preferredProperties match {
      case Some((_, parent, props)) =>
        val result = new Properties()
        result.putAll(loadProperties(Locale.forLanguageTag(parent))._4)
        result.putAll(props)
        new Resource(result)
      case None =>
        new Resource(loadProperties(defaultLocale)._4)
    }
  }

  /**
   * Gets the resource from this bundle associated with the given locale.
   * If locale is not supported, or if no locale is given, return the resource
   * for the default locale.
   * @param locale the preferred locale
   */
  def forLocale(locale: Locale): Resource = {
    if (locale == null) {
      forLocale("")
    }
    else {
      forLocale(locale.toString.replaceAllLiterally("_", "-"))
    }
  }

  private def rangeToLocales(localeRange: String): List[Locale] = {
    if (isBlank(localeRange)) {
      List(defaultLocale)
    }
    else {
      val ranges = Locale.LanguageRange.parse(localeRange).asScala
      if (ranges != null && ranges.nonEmpty) {
        ranges.map(range => Locale.forLanguageTag(range.getRange)).toList
      }
      else {
        List(defaultLocale)
      }
    }
  }

  private def loadProperties(locale: Locale): (Locale, Int, String, Properties) = {
    var language: Locale = null
    var priority: Int = 0
    var parent: String = ""
    var result: Option[Properties] = None

    if (locale == defaultLocale || isBlank(locale.toString)) {
      priority = 1
      parent = ""
      val found = findProperties(locale, defaultLocale, s"$bundle.properties")
      if (found.isDefined) {
        language = found.get._1
        result = Option(found.get._2)
      }
    }

    if (result.isEmpty && !isBlank(locale.getLanguage) && !isBlank(locale.getCountry)) {
      priority = 1
      parent = locale.getLanguage
      val found = findProperties(locale, locale, s"${bundle}_${locale.getLanguage}_${locale.getCountry}.properties")
      if (found.isDefined) {
        language = found.get._1
        result = Option(found.get._2)
      }
    }

    if (result.isEmpty && !isBlank(locale.getLanguage)) {
      priority = 2
      parent = locale.getLanguage
      val found = findProperties(locale, Locale.forLanguageTag(locale.getLanguage), s"${bundle}_${locale.getLanguage}.properties")
      if (found.isDefined) {
        language = found.get._1
        result = Option(found.get._2)
      }
    }

    result match {
      case Some(props) =>
        (language, priority, parent, props)
      case None =>
        (null, 99, "", new Properties())
    }
  }

  private def findProperties(requestedLocale: Locale, actualLocale: Locale, fileName: String): Option[(Locale, Properties)] = {
    if (enableCache && cache.containsKey(requestedLocale)) {
      Some(cache.get(requestedLocale))
    }
    else {
      val url = fetchFile(fileName)
      if (url != null) {
        val props = new Properties()
        var reader: InputStreamReader = null
        try {
          reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)
          props.load(reader)
          if (enableCache) {
            cache.put(requestedLocale, (actualLocale, props))
          }
          Some((actualLocale, props))
        }
        catch {
          case NonFatal(_) =>
            None
        }
        finally {
          Try(reader.close())
        }
      }
      else {
        None
      }
    }
  }

  private def isBlank(str: String): Boolean = {
    str == null || str.trim.isEmpty
  }

}