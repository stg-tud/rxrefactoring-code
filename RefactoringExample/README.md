# Juneiform
App downloaded from: https://bitbucket.org/Stepuk/juneiform

This app is used to show an example of how one can use **SwingWorker2Rx**.

## Setup

Install Cuneiform
- sudo apt-get install cuneiform

## Testing the app
You can use the file _Document.jpg_ from the root folder to test the OCR

## Projects
This repository contains 4 versions of Juneiform.
- **Juneiform_1_Original**: original code
- **Juneiform_2_Modified**: DocumentLoader was slightly modified in order to
load the images into the application as soon as they are available rather
than waiting that all of them are in memory and loading all of them at once
at the end.
- **Juneiform_3_Automated_Refactoring**: source code after using **SwingWorker2Rx**
to refactor Juneiform.
- **Juneiform_4_New_Features**: new features were added in a functional fashion
using RxJava constructs.