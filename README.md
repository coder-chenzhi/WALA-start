WALA and TinyPDG Starter Kit
=======

### Introduction

This is an example project to slice program using both WALA and TinyPDG.
[WALA](https://github.com/wala/WALA) interprocedural tool for program analysis framework.  And [TinyODG] (https://github.com/YoshikiHigo/TinyPDG) intraprocedural library for program analysis. Note: we have updated or add some interface and API in WALA and TinyPDG to perform program slicing. You can clone and build this project to get WALA installed, and then modify it to suit your own needs.

### Requirements

Requirements are:

  * Java 8
  * The [Gradle](https://gradle.org/) build tool

On Mac OS X, you can install these requirements by installing
[Homebrew](https://brew.sh/) and then running:

    brew cask install java
    brew install gradle
    
### Installation

Clone the repository, and then run:

    ./gradlew compileJava
    
This will pull in the WALA jars and build the sample code.

### Example setup
(1) In the folder dat, setup the analysis scope in the file scope.txt. The example will analyze the source code and compiled class files.
(2) Import your source code and class file into the dat folder or other specific dir.
(3) You can use the default exclusions or change by yourself.
(4) Run the slice main entry: slice/sliceMain.java.
  

License
-------

All code is available under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html).
