package com.linearframework.i18n

import java.net.URL

object ResourceUtils {

  /**
   * Gets the given resource from the classpath
   * @param resource relative path to the resource
   */
  def getClasspathResource(resource: String): URL = {
    var url = Thread.currentThread.getContextClassLoader.getResource(resource)
    if (url == null)
      url = this.getClass.getClassLoader.getResource(resource)
    if (url == null)
      url = this.getClass.getResource(resource)
    url
  }

}
