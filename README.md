# kotcot

[![](https://jitpack.io/v/cyberpython/kotcot.svg)](https://jitpack.io/#cyberpython/kotcot)

**Kot**lin **C**ursor-**o**n-**T**arget (CoT) library.

KotCoT can parse the base CoT v2.0 event as well as extract values from the `detail` extension node.

A utility class (`CoTType2SIDC`) is included to convert from CoT type to MILSTD-2525B SIDCs and the 
reverse.

Refer to the unit tests for usage examples.

The library uses Jackson and Woodstox for XML handling.

Any object can be set as the `detail` for Event objects and it will be serialized by Jackson, but it
will serialize all fields and values as nested elements. If you want to customize this behaviour and
specify fields to be used as attributes in the generated XML, you need to create custom classes 
(data classes are recommended) annotated with Jackson's annotations.

The CoT Event XSD schema that the development of the library is based on can be found under `/doc/`.

