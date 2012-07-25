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

* `scala-ide-2.0-scala-2.9`
* `scala-ide-2.0.x-scala-2.9`
* `scala-ide-master-scala-2.9`
* `scala-ide-master-scala-trunk`

Run maven like this:

    mvn -P scala-ide-master-scala-trunk clean install
