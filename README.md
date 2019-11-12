
# Refactoring to Reactive Programming

2Rx is a refactoring approach to automatically
convert asynchronous code to reactive programming.

Reactive programming languages and libraries, such as
ReactiveX, have been shown to significantly improve software
design. Rx supports refactoring existing software that has been designed with
traditional abstractions for asynchronous programming to the reactive paradigm.


## Rx in a Nutshell

Recently, Reactive Programming (RP) has emerged as a programming
paradigm specifically addressing software that combines
events. Crucially, RP allows to easily express computations on event streams
that can be chained and combined using high-order functional operators.
This way, each operator can be scheduled independently, providing a convenient
model for asynchronous programming.
As a result, RP provides means to describe asynchronous programs
in a declarative way.

The benefits of RP design are widely recognized, and
new projects can easily adopt RP abstractions right away, but this technology
is not portable to existing software without manual refactoring.

Rx is an approach to automatically
refactoring asynchronous code to the RP style based on [ReactiveX](http://reactivex.io/)'s `Observable`,
which is a programming abstraction that emits
events that are asynchronously handled by `Observers`. [ReactiveX](http://reactivex.io/) uses
operators from RP to compose computations on `Observable`s.
Using these operators, it is straight-forward to extend asynchronous computations,
adding new operators such as [`map`](http://reactivex.io/documentation/operators/map.html):

![](https://stg-tud.github.io/rxrefactoring-code/map.png)

Our methodology applies to common abstractions for 
asynchronous computations in Java, including `Future`, and Swing's `SwingWorker`.
Rx has been tested on more than 7K third-party popular GitHub projects, 
including Apache Zookeeper, Jabref, JUnit, and Mockito, showing the broad applicability of our technique.


# Datasets

The global and refactoring datasets are available at:
https://github.com/stg-tud/rxrefactoring-dataset

# Eclipse Plugin

Rx is an Eclipse plugin that provides refactoring for asynchronous
constructs to ReactiveX. 

![](https://stg-tud.github.io/rxrefactoring-code/image.png)

# Repository Overview

This repository contains a core project which provides
general refactoring functionality and integration into Eclipse.
This project can be extended to implement Refactoring solutions
for various asynchronous constructs, e.g., Java Future.

## Structure

- __de.tudarmstadt.rxrefactoring.core__: The extensible core project.
- __de.tudarmstadt.rxrefactoring.ext.XXX__: The extensions for each
supported asynchronous construct XXX.

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

     
