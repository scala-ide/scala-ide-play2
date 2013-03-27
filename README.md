# Scala IDE plugin for Play 2.1

This is an extension to Scala IDE to support Play 2.1 routes and template files.

For user documentation, check the [wiki](https://github.com/scala-ide/scala-ide-play2/wiki).

## Project structure

The project is composed of 5 Eclipse plugins:

* the core plugin
* the test plugin
* an Eclipse feature
* an Eclipse source feature
* an Eclipse update-site

The projects can be imported inside Scala IDE. And they are fully configured to be compiled with maven and tycho.

## Building

The maven build is configured using a combination of 2 profiles:

* An Eclipse platform profile:
  * `eclipse-indigo`
  * `eclipse-juno`

* A Scala IDE version profile:
  * `scala-ide-stable`
  * `scala-ide-dev`
  * `scala-ide-nightly`

After choosing the flavor you wish to build, maven is run using:

    mvn -Peclipse-indigo -Pscala-ide-stable clean package
