package com.linearframework.i18n

import com.linearframework.BaseSpec
import java.util.Locale

class ResourceBundleSpec extends BaseSpec {

  private lazy val badBundle = ResourceBundle.of("I_DONT_EXIST")
  private lazy val cachedBundle = ResourceBundle.of("testBundle")
  private lazy val unCachedBundle = ResourceBundle.of("testBundle", cached = false)

  "A poorly-defined resource" should "always return None" in {
    val resource = badBundle.forLocale("en-GB;q=1.0,en;q=0.75,en-US;q=0.5,fr-FR;q=0.25")
    resource("hello") should be (None)
  }

  "A resource bundle" should "return the bundle for the default locale if none is specified" in {
    cachedBundle.forLocale(null.asInstanceOf[String]).apply("hello", "Steve").get should be ("Hello, Steve!")
    cachedBundle.forLocale(null.asInstanceOf[Locale]).apply("hello", "Steve").get should be ("Hello, Steve!")
    cachedBundle.forLocale("").apply("hello", "Steve").get should be ("Hello, Steve!")
    cachedBundle.forLocale(",").apply("hello", "Steve").get should be ("Hello, Steve!")
  }

  it should "return the resource for the default locale if none of the locales in the provided range are supported" in {
    val rangeResource = cachedBundle.forLocale("fr-FR;q=1.0,fr;q=0.75")
    val localeResource = cachedBundle.forLocale(Locale.FRANCE)

    rangeResource("hello", "Jean-Luc").get should be ("Hello, Jean-Luc!")
    localeResource("hello", "Jean-Luc").get should be ("Hello, Jean-Luc!")
  }

  it should "return the resource for the first fully-supported locale in the provided range" in {
    val rangeResource = cachedBundle.forLocale("es-MX;q=1.0,es-AR;q=0.75,en;q=0.25")
    val localeResource = cachedBundle.forLocale(Locale.forLanguageTag("es-AR"))

    rangeResource("hello", "Carlos").get should be ("¡Hola Carlos!")
    rangeResource("goodbye").get should be ("¡Chau!")
    localeResource("hello", "Carlos").get should be ("¡Hola Carlos!")
    localeResource("goodbye").get should be ("¡Chau!")
  }

  it should "return the resource for the first partially-supported locale in the provided range if no locales are fully-supported" in {
    val rangeResource = cachedBundle.forLocale("es-MX;q=1.0,en;q=0.25")
    val localeResource = cachedBundle.forLocale(Locale.forLanguageTag("es-MX"))

    rangeResource("hello", "Sofía").get should be ("¡Hola Sofía!")
    rangeResource("goodbye").get should be ("¡Adiós!")
    localeResource("hello", "Sofía").get should be ("¡Hola Sofía!")
    localeResource("goodbye").get should be ("¡Adiós!")
  }

  "A cached bundle" should "be faster than an uncached bundle" in {
    val cycles = 10000

    val uncachedStartTime = System.currentTimeMillis()
    (1 to cycles).foreach(_ => unCachedBundle.forLocale("es-AR").apply("goodbye").get should be ("¡Chau!"))
    val uncachedEndTime = System.currentTimeMillis()

    val cachedStartTime = System.currentTimeMillis()
    (1 to cycles).foreach(_ => cachedBundle.forLocale("es-AR").apply("goodbye").get should be ("¡Chau!"))
    val cachedEndTime = System.currentTimeMillis()

    val uncachedTime = uncachedEndTime - uncachedStartTime
    val cachedTime = cachedEndTime - cachedStartTime

    (cachedTime < uncachedTime) should be (true)
  }

}
