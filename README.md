# refactoring-rp3

        Automated Refactoring of Asynchronous Applications
    IMPL Project and Bachelor thesis Grebiel José Ifill Brito

## Overview

This repository contains the source code of 2Rx, an Eclipse plugin
that supports extension for automated refactoring of Java Projects.

Furthermore, you find here the source code of SwingWorker2Rx. This
is the extension of 2Rx responsible for refactoring SwingWorkers
into an implementation that uses the ReactiveX API for Java (RxJava).

Additionally, we took the implementation of RxFactor and adapted to
be a client of 2Rx as well (AsyncTask2Rx).

## Structure

- Archive: This directory contains out of dated data that was use
  during development. Particularly, it contains some example applications
  and the first refactoring suggestions. The final refactoring is far
  away from the ones shown in these projects. Therefore, this directory
  can be ignored or completely removed.

- Documentation: PDF and TEX files. The PDF provides a background about
  asynchronous programming and refactoring. Moreover, it explains the
  motivation and contribution, the refactoring approach used in
  SwingWorker2Rx plus details about the design and implementation of
  the system. This document also contains the evaluation details and its
  results and some suggestions for future research.

- RxJavaRefactoringCore: 2Rx Project

- RxJavaRefactoringExtensionSwingWorker: SwingWorker2Rx Project

- RxJavaRefactoringExtensionAsyncTask: AsyncTask2Rx Project (based on RxFactor)

- RxJavaRefactoringExtensionTemplate: Template for implementing further
  extensions

- RxRefactoringTestsSwingWorker: Project used for the Unit-Tests of SwingWorker2Rx

- RxRefactoringTestTemplate: Template for writing unit-test when
  developing new extensions.

- swing-worker-to-rx: RxJava extension to refactor SwingWorkers as a
  Maven project.

## Setup

In order to test 2Rx and any of its extensions, you must import
2Rx and the corresponding extension(s) into the Eclipse workspace.

I.e: RxJavaRefactoringCore and RxJavaRefactoringExtensionSwingWorker

Then you can click on the arrow next to the run or debug button, and
select "Eclipse Application". This should open a second instance of
Eclipse. There you will see a new menu called "2Rx". When you click
there you will see the name of all extensions imported into the
workspace. Assuming you have imported AsyncTask2Rx and SwingWorker2Rx,
then you would see the following menu entries:
    - AsyncTask to RxJava
    - SwingWorker to RxJava

Select one option to start the refactoring. The refactoring will be done
in all opened Java projects. If you would like to exclude a project,
then you can right-click on it and select "Close Project" from the
context menu (you don't need to remove the project from the workspace).

Please notice that 2Rx do NOT support UNDO. Please do a backup of your
projects before starting the refactoring! A good option is to have
them in a repository, so you can always quickly revert the changes.

## Running Tests

In order to run the Tests, right-click on the test project, i.e:
RxRefactoringTestsSwingWorker, and select run or debug as
"JUnit Plug-in Test". After having done that one time, you
will see this option in the toolbar too.