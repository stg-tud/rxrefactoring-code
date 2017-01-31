# refactoring-rp3
IMPL Project and Bachelor thesis Grebiel José Ifill Brito

Automated Refactoring of Asynchronous Applications
(SwingWorkers to RxJava)

## Overview

This repository contains the source code of __2Rx__, an Eclipse plugin
that supports extension for automated refactoring of Java Projects.

Furthermore, you find here the source code of __SwingWorker2Rx__. This
is the extension of __2Rx__ responsible for refactoring __SwingWorker__s
into an implementation that uses the __ReactiveX API__ for Java (__RxJava__).

Additionally, we took the implementation of __RxFactor__ and adapted to
be a client of __2Rx__ as well (__AsyncTask2Rx__).

## Structure

- __Archive__: This directory contains out of dated data that was use
  during development. Particularly, it contains some example applications
  and the first refactoring suggestions. The final refactoring is far
  away from the ones shown in these projects. Therefore, this directory
  can be ignored or completely removed.

- __Documentation__: PDF and TEX files. The PDF provides a background about
  asynchronous programming and refactoring. Moreover, it explains the
  motivation and contribution, the refactoring approach used in
  SwingWorker2Rx plus details about the design and implementation of
  the system. This document also contains the evaluation details and its
  results and some suggestions for future research.

- __RxJavaRefactoringCore__: 2Rx Project

- __RxJavaRefactoringExtensionSwingWorker__: SwingWorker2Rx Project

- __RxJavaRefactoringExtensionAsyncTask__: AsyncTask2Rx Project (based on RxFactor)

- __RxJavaRefactoringExtensionTemplate__: Template for implementing further
  extensions

- __RxRefactoringTestsSwingWorker__: Project used for the Unit-Tests of SwingWorker2Rx

- __RxRefactoringTestTemplate__: Template for writing unit-test when
  developing new extensions.

- __swing-worker-to-rx__: RxJava extension to refactor SwingWorkers as a
  Maven project.

## Setup

In order to test __2Rx__ and any of its extensions, you must import
__2Rx__ and the corresponding extension(s) into the Eclipse workspace.

I.e: RxJavaRefactoringCore and RxJavaRefactoringExtensionSwingWorker

Then you can click on the arrow next to the run or debug button, and
select __Eclipse Application__. This should open a second instance of
Eclipse. There you will see a new menu called __2Rx__. When you click
there you will see the name of all extensions imported into the
workspace. Assuming you have imported __AsyncTask2Rx__ and __SwingWorker2Rx__,
then you would see the following menu entries:
    - AsyncTask to RxJava
    - SwingWorker to RxJava

Select one option to start the refactoring. The refactoring will be done
in all opened Java projects. If you would like to exclude a project,
then you can right-click on it and select "Close Project" from the
context menu (you don't need to remove the project from the workspace).

Please notice that __2Rx__ do __NOT__ support __UNDO__. Please do a backup of your
projects before starting the refactoring! A good option is to have
them in a repository, so you can always quickly revert the changes.

## Running Tests

In order to run the Tests, right-click on the test project, i.e:
RxRefactoringTestsSwingWorker, and select run or debug as
__JUnit Plug-in Test__. After having done that one time, you
will see this option in the toolbar too.
