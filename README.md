
# Refactoring to Reactive Programming

Reactive programming languages and libraries, such as
ReactiveX, have been shown to significantly improve software
design and have seen important industrial adoption.
Asynchronous applications - which are notoriously error-prone to
implement and to maintain - greatly benefit from
reactive programming because they can be defined in a declarative style,
which improves code clarity and extensibility.

In this project, we tackle the problem of
refactoring existing software that has been designed with
traditional abstractions for asynchronous programming.

2Rx is a refactoring approach to automatically
convert asynchronous code to reactive programming.
We tested 2Rx on top-starred GitHub projects showing that 
effective with common asynchronous constructs
and it can provide a refactoring for 91.7% of their occurrences.

# Eclipse Plugin

Rx is an Eclipse plugin that provides refactoring for asynchronous
constructs to ReactiveX. 

![](https://stg-tud.github.io/rxrefactoring-code/image.png)

## Repository Overview

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

# Publications

Mirko Köhler, Guido Salvaneschi, Automated Refactoring to Reactive Programming,
34th IEEE/ACM International Conference on Automated Software Engineering (ASE 2019). 

     
