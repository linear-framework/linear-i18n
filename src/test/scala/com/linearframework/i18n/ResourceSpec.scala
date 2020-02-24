package com.linearframework.i18n

import com.linearframework.BaseSpec
import java.util.Properties

class ResourceSpec extends BaseSpec {

  val props: Properties = {
    val file = ResourceUtils.getClasspathResource("resource.properties")
    val p = new Properties()
    p.load(file.openStream())
    p
  }

  val resource: Resource = new Resource(props)

  "A resource" should "return None if the given key is not found" in {
    resource("bad.prop") should be (None)
  }

  it should "return the value of the requested property" in {
    resource("hello") should be (Some("Hello"))
  }

  it should "format the return value if format objects are provided" in {
    resource("how.are.you", "Steve", "good") should be (Some("How are you, Steve? I am good."))
    resource("how.are.you", "Billy", "great") should be (Some("How are you, Billy? I am great."))
  }

}
