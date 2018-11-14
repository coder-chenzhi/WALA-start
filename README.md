WALA and TinyPDG Starter Kit
=======

### Introduction

This is an example project to slice program using both WALA and TinyPDG.
[WALA](https://github.com/wala/WALA) is an interprocedural tool for program analysis framework.  And [TinyPDG](https://github.com/YoshikiHigo/TinyPDG) is an intraprocedural library for program analysis. Note: we have updated or added some interfaces or APIs in WALA and TinyPDG to perform program slicing for our specific requirement. You can clone and build this project and then modify it to suit your own needs.

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

  * In the folder dat, setup the analysis scope in the file scope.txt. The example will analyze the source code and compiled class files.
  * Import your source code and class file into the dat folder or other specific dir.
  * You can use the default exclusions or change by yourself.
  * Run the slice main entry: slice/sliceMain.java.
  

License
-------

All code is available under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html).
