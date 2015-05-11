# Scala IDE plugin for Play 2.x

This is an extension to Scala IDE to support Play 2.x routes and template files.

For user documentation, check the [wiki](https://github.com/scala-ide/scala-ide-play2/wiki).

# Supported Play versions

This plugin provides Eclipse supports for Play 2.3.x projects. However, it should also work with older Play releases.

## Project structure

The project is composed of 5 Eclipse plugins:

* the core plugin
* the test plugin
* an Eclipse feature
* an Eclipse source feature
* an Eclipse update-site

The projects can be imported inside Scala IDE. And they are fully configured to be compiled with maven and tycho.

## Build

Simply run the ``./build.sh`` script.

The build is based on
[plugin-profiles](https://github.com/scala-ide/plugin-profiles) and
can be built against several versions of the IDE and Eclipse.
