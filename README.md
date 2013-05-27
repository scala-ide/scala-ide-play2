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

## Build

Simply run the ``./build.sh`` script.

The build is based on
[plugin-profiles](https://github.com/scala-ide/plugin-profiles) and
can be built against several versions of the IDE and Eclipse.

Launching the ``build.sh`` script will exand to the following Maven command:

```
mvn -Peclipse-indigo,scala-2.10.x,scala-ide-stable clean install
```

You can choose a different profile for any of the three axis: eclipse
platform, and Scala IDE stream. The Scala profile has to be ``scala-2.10.x`` because 
this plug-in targets Play v2.1, which is only available for Scala 2.10. 
Read [here](https://github.com/scala-ide/scala-worksheet/wiki/Build-the-Worksheet)
for more details.
