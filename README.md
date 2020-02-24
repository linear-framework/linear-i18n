# linear-i18n
Internationalization module of the **Linear Framework**.

## API
The i18n API uses the concept of `ResourceBundle`s.  

### Quick Demo
```scala
import com.linearframework.i18n._

val messages = ResourceBundle.of("messages") 

val english = messages.forLocale(java.util.Locale.US)
val spanish = messages.forLocale("es-MX;q=1.0,es-AR;q=0.75,en;q=0.25")

english("goodbye") // "Goodbye!"
spanish("goodbye") // "¡Adiós!"
```

### Creating Resource Bundles
A `ResourceBundle` is nothing more than a collection of property files that share the same base name,
but vary on language and country/region (e.g., `messages.properties`, `messages_es.properties`, and 
`messages_es_MX.properties`).

ResourceBundles can be created with `ResourceBundle.of()`, which has a few optional configuration parameters:
```scala
val stringsBundle = 
  ResourceBundle.of(
    // (required) the group of property files to load
    bundleName = "strings",
    
    // (optional) sets the fallback locale (the language of the base property file); 
    //            default is "Locale.US"
    fallback = Locale.GERMAN, 
    
    // (optional) should the underlying properties file be cached in memory?; 
    //            default is true 
    cached = false, 
    
    // (optional) defines how to construct a URL from a property file name; 
    //            default behavior is to get a URL from the classpath
    getFileUrl = { propertyFileName => 
      new File(s"/Users/billy/props/$propertyFileName").toURI.toURL
    }
  )
``` 

### Fetching Locale-Specific Resources
With a `ResourceBundle` in hand, resources for various locales can be loaded in one of two ways:
 - `bundle.forLocale(<java.util.Locale>)`
   - Loads the resource specific to the given Java Locale object
   - If the Locale is specific to a country/region, the correlating country/region-specific properties will be 
     loaded if available.  If not available, the language-specific properties will be loaded.
   - If no properties are available which match the language of the given Locale, properties for
     the default Locale will be returned.
 - `bundle.forLocale(<locale range>)`
   - Parses the given locale range as if it is an `Accept-Language` header 
     ([see here](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language)).
   - For each locale, in order of preference, resources are attempted to be loaded until one is found.
   - If no properties are available which match the language of any locales in the range, properties for
     the default Locale will be returned.

Country/region-specific resources inherit from their parent language 
(e.g., both `es-MX` and `es-AR` inherit all the properties of `es`).


### Getting values from a Resource
Values can be fetched from a `Resource` in two ways: with or without String formatting applied.

Consider the following properties file:
```properties
greeting.hello = Hello, %s! Happy %s!
```

This greeting can either be fetched as-is:
```scala
// "Hello, %s! Happy %s!"
bundle("greeting.hello") 
```

Or it can be fetched with formatting applied automatically:
```scala
// "Hello, Billy! Happy Wednesday!"
bundle("greeting.hello", user.firstName, dayOfWeek) 
```


### Linear vs Java
Linear's `ResourceBundle` addresses slightly different needs than Java's.
Here's a quick table to help decide which tool is right for you: 

| Feature                                                            | Linear  | Java    |
|:-------------------------------------------------------------------|:-------:|:-------:|
| `.properties` files support                                        | &#9745; | &#9745; |
| Coded properties (`ListResourceBundle`) support                    | &#9744; | &#9745; |
| Multiple languages                                                 | &#9745; | &#9745; |
| Multiple countries/regions for each language                       | &#9745; | &#9745; |
| Variants within each country/region                                | &#9744;*| &#9745; |
| Countries/regions inherit properties from parent language          | &#9745; | &#9745; |
| Fallback to default locale instead of throwing Exception           | &#9745; | &#9744; |
| Out-of-the-box support for locale range (`Accept-Language` header) | &#9745; | &#9744; |
| Out-of-the-box support for format strings (`"Hello, %s!"`)         | &#9745; | &#9744; |

_*may be available in a future version_