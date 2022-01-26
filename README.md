# Java Utility Functions

This library is a collection of utility functions that I've written over the years. It exists [because I found
myself rewriting the same classes for each job](https://blog.kdgregory.com/2009/12/why-write-open-source-libraries.html).
At present I don't use Java much, so updates are few and far between.

The library currently supports JDK 1.5 -- I still have a 1.5 JVM installed on my main development box.
However, this is the last version that will do so. The next release will use Java8, and will remove any
classes/methods that replicate functionality found in Java8. This version deprecates everything that
will be removed.


## Usage

Include the following dependency (variants for other build tools and the latest version number is [available
from Maven Central](https://search.maven.org/search?q=a:kdgcommons):

```
<dependency>
  <groupId>net.sf.kdgcommons</groupId>
  <artifactId>kdgcommons</artifactId>
  <version>LATEST_VERSION</version>
</dependency>
```

Decide what classes are useful to you.

Profit!


## Structure

The top-level package is `net.sf.kdgcommons`. Under that you'll find the following packages:

 Name           | Purpose
----------------|---------
`bean`          | An introspection library for bean-style data classes that lets you manage the cache (so you don't end up with strong references to things you thought you unloaded).
`buffer`        | Abstractions over `java.nio.ByteBuffer`. See [my article](https://www.kdgregory.com/index.php?page=java.byteBuffer) for detailed information on ByteBuffers.
`codec`         | Classes that convert byte arrays to and from another format (currently, Base64 and Hex).
`collections`   | Operations for building and using Java collections.
`html`          | Utilities for extracting and constructing HTML and URLs, ignoring everything I've ever said about not treating HTML as text.
`io`            | Streams and utilities for working with them. A lot of overlap with Apache Commons IO (including one class that ended up living there, albeit transformed beyond recognition).
`lang`          | Tools for working with core Java objects, especially strings and classes.
`net`           | Constants, including MIME types and standard HTTP headers. In case you don't want to include Apache HTTP Components.
`sql`           | At present, only `JDBCUtil`, which executes JDBC operations within a try-catch. This may be removed in the next addition.
`test`          | Helpers for JUnit tests, including a range of asserts and mocks. Supports JUnit versions from 3.8.2 up.
`tuple`         | At present, just supports 2-tuples.
`util`          | A catch-all for everything else. Looking at it as I write this README, most of what it contains should be elsewhere.
