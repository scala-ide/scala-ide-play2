scala-ide-plugin.g8
===================

Giger8 template for Eclipse plugins based on the Scala IDE.

This template produces 5 Eclipse plugins:

* the plugin itself
* the `plugin.tests` fragment
* an Eclipse feature
* an Eclipse source feature
* an Eclipse update-site

The projects can readily be imported inside Eclipse. Additionally, you have maven `pom` files
based on Tycho, enabling command line builds.

## Note:

There is no default profile. You need to specify a profile manually, choosing what version
of the Scala IDE and Scala compiler you want to build against:

* `scala-ide-milestone-indigo-scala-2.9`
* `scala-ide-milestone-juno-scala-2.9`
* `scala-ide-milestone-indigo-scala-2.10`
* `scala-ide-milestone-juno-scala-2.10`

Run maven like this:

    mvn -P scala-ide-milestone-juno-scala-2.10 clean install
