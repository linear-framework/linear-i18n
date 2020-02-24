package com.linearframework.i18n

import com.linearframework.BaseSpec

class ResourceUtilsSpec extends BaseSpec {

  "getClasspathResource()" should "get the URL of a file on the classpath, if available" in {
    ResourceUtils.getClasspathResource("file.txt") should not be (null)
    ResourceUtils.getClasspathResource("directory/file.txt") should not be (null)
    ResourceUtils.getClasspathResource("directory/nope.txt") should be (null)
  }

}
