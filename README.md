# kotcot
![Build](https://github.com/cyberpython/kotcot/actions/workflows/main.yml/badge.svg)
![Coverage](.github/badges/jacoco.svg)
[![](https://jitpack.io/v/cyberpython/kotcot.svg)](https://jitpack.io/#cyberpython/kotcot)

**Kot**lin **C**ursor-**o**n-**T**arget (CoT) library.

KotCoT can parse the base CoT v2.0 event as well as extract values from the `detail` extension node.

A utility class (`CoTType2SIDC`) is included to convert from CoT type to MILSTD-2525B SIDCs and the 
reverse.

The library uses Jackson and Woodstox for XML handling.

The CoT Event XSD schema that the development of the library is based on can be found under `/doc/`.

## Installation

The library can be installed from JitPack.

If you are using Gradle, add JitPack to the available repositories and then
add KotCot to the dependencies of your project:

`build.gradle:`
```
...

repositories {
  ...
  maven { url 'https://jitpack.io' }
}

...

dependencies {
  ...
  implementation 'com.github.cyberpython:kotcot:v1.0.2
  ...
}
...
```

If you are using Maven, update your `pom.xml` in a similar manner:

`pom.xml:`
```
...

<repositories>
  <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
  </repository>
</repositories>

...

<dependency>
    <groupId>com.github.cyberpython</groupId>
    <artifactId>kotcot</artifactId>
    <version>v1.0.2</version>
</dependency>

...
```

# Reading & writing CoT Event objects

Reading (parsing) CoT Event objects from XML is quite straightforward:

``` kotlin
val cot = CoT()
val evt = cot.parse(xml)

// evt now holds the CoT Event's data
```

Things to note is that:

- In order to acces the MILSTD-2525B SIDC symbol code embedded in the CoT 
  Event's `type` field, you can use the `CoTType2SIDC::to2525B` method, 
  passing the CoT Event's type as a parameter:
  ``` kotlin
  val sidc = CoTType2SIDC().to2525B(evt.type)
  ```
- The `detail` field of the CoT Event and its sub-items can be accessed using 
  dot-notation through the `Event::getDetail` method:
  ``` kotlin
  val vesselName = evt.getDetail("vessel.name")
  ```

Serializing an Event object to XML is straightforward as well:
``` kotlin
// Create the CoT Event object:
val event = Event(Point(37.409, 23.768, 0.0, 0.0, 0.0), null, 2, 
                  CoTType2SIDC().from2525B("SFGPUCVRA------"), "public", "1-r-c",
                  "4333319d-3430-44f3-bada-195fcc9bf349", 
                  ZonedDateTime.parse("2022-08-20T18:50:10Z"), 
                  ZonedDateTime.parse("2022-08-20T18:50:10Z"), 
                  ZonedDateTime.parse("2022-08-20T19:00:10Z"), "m-g")
// Write to XML:
val cot = CoT()
val xml = cot.write(event)
```

Any object can be set as the `detail` for Event objects and it will be 
serialized by Jackson. Note that Jackson will serialize all fields and values 
as nested elements. If you wish to customize this behaviour and specify fields 
to be used as attributes in the generated XML, you need to create custom classes 
(data classes are recommended) annotated with Jackson's `@JacksonXmlProperty` 
annotation.


Refer to the unit tests for more usage examples.
