# Java Utility Functions

This library is a collection of utility functions that I've written over the years.
It exists because [I found myself rewriting the same classes for each company that
I worked with](https://blog.kdgregory.com/2009/12/why-write-open-source-libraries.html).
At present I don't use Java much, so updates are few and far between.

The library currently supports Java 8 and higher. If you are using an earlier Java version,
you can use [version 1.x](https://github.com/kdgregory/kdgcommons/tree/1.0.19) of the library,
but it will not receive updates.


## Usage

Include the following dependency in your project (variants for other build tools and the latest
version number is [available from Maven Central](https://search.maven.org/search?q=a:kdgcommons)):

```
<dependency>
  <groupId>com.kdgregory.util</groupId>
  <artifactId>kdgcommons</artifactId>
  <version>LATEST_VERSION</version>
</dependency>
```

Decide what classes are useful to you (JavaDoc is available [here](https://kdgregory.github.io/kdgcommons/apidocs/index.html)).

Profit!


## Structure

The top-level package is `com.kdgregory.kdgcommons`. Under that you'll find the following packages:

 Name           | Purpose
----------------|---------
`bean`          | An introspection library for bean-style data classes that lets you manage the cache (so you don't end up with strong references to things you thought you unloaded).
`buffer`        | Abstractions over `java.nio.ByteBuffer`. See [my article](https://www.kdgregory.com/index.php?page=java.byteBuffer) for detailed information on ByteBuffers.
`codec`         | Classes that convert byte arrays to and from another format (currently, Base64 and Hex).
`collections`   | Operations for building and using Java collections.
`io`            | Streams and utilities for working with them. A lot of overlap with Apache Commons IO (including one class that ended up living there, albeit transformed beyond recognition).
`lang`          | Tools for working with core Java objects, especially strings and classes.
`sql`           | At present, only `JDBCUtil`, which executes JDBC operations within a try-catch. This may be removed in the next addition.
`test`          | Helpers for JUnit tests, including a range of asserts and mocks. Supports JUnit versions from 3.8.2 up.
`tuple`         | At present, just supports 2-tuples.
`util`          | A catch-all for everything else. Looking at it as I write this README, most of what it contains should be elsewhere.


## Versions

I use the standard `major.minor.patch` format:

* `major` is currently (and expected to stay) 2
* `minor` is updated whenever I add new classes or packages (in practice,
  it's updated with every release other than a bugfix, since I accumulate
  changes).
* `patch` is updated for bugfixes or minor additions to existing classes.

The API will remain backwards-compatible for all 2.x releases, although I
may choose to deprecate functionality.
  

## Migrating from version 1.x

I decided to make a clean break with the earlier version, so that transitive
dependencies (for example, from [practicalxml](http://practicalxml.sourceforge.net/))
would not be affected:

* A new Maven group ID: `com.kdgregory.util` versus `net.sf.kdgcommons`.
* New package names: `com.kdgregory.kdgcommons` versus `net.sf.kdgcommons`

The easiest way to upgrade is start by using the latest 1.x release, 1.0.19. This
release deprecated all functionality that was removed in 2.x (mostly operations
that are better implemented using Java8 lambdas). Run a test compile to find any
deprecations, fix them, and look at the
[changelog](https://kdgregory.github.io/kdgcommons/changes-report.html#a2.0.0)
to see if you'll be affected by any of the minor changes.

After that, using the new version should be a simple change of Maven dependencies
and imports.
