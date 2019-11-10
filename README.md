
# Refactoring to Reactive Programming

The documentation for this project is available in the [Project's website](https://stg-tud.github.io/rxrefactoring-code/)

2Rx is an Eclipse plugin that provides refactoring for asynchronous
constructs to ReactiveX. 

## Overview

This repository contains a core project which provides
general refactoring functionality and integration into Eclipse.
This project can be extended to implement Refactoring solutions
for various asynchronous constructs, e.g., Java Future.

## Structure

- __de.tudarmstadt.rxrefactoring.core__: The extensible core project.
- __de.tudarmstadt.rxrefactoring.ext.XXX__: The extensions for each
supported asynchronous construct XXX.

## Develop

develop - Branch for development.


## Automatic Testing

The system provides a framework for automatically testing the refactored code.
The testing is done as follows:

1. Use the Refactoring Tool. The new folder in /tmp/randoop-gen... is created. 
It contains the necessary automatic testing system.
2. Copy all project libaries, JUnit, Hamcrest, ReactiveX, Reactive-Streams and Randoop as jar-files 
to /tmp/randoop-gen.../libs. The common libraries are in automatic-testing/libs.
3. The script /tmp/randoop-gen.../randoop.sh generates JUnit tests and executes them. 

