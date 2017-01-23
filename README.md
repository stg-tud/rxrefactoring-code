# refactoring-rp3
IMPL Project and Bachelor thesis Grebiel José Ifill Brito




In order to test the tool both projects, core and extension, are needed. 
Please import both projects. The relevant folders from the repo are:

https://github.com/allprojects/refactoring-rp3/tree/master/RxJavaRefactoringCore

https://github.com/allprojects/refactoring-rp3/tree/master/RxJavaRefactoringExtensionSwingWorker

After importing those 2 projects into eclipse, they should compile. 
Then please select run as a eclipse application. 
A second instance of eclipse should be opened. 
This second instance has a new mene - RxJava Refactorings. 
When you click there, then you can select "SwingWorker to RxJava". 

In case one wants to try the AsyncTasks integration then the project 

https://github.com/allprojects/refactoring-rp3/tree/master/RxJavaRefactoringExtensionAsyncTask 

should be imported as well. 

I haven't though yet evaluate if this extension is exactly doing what Ram's tool did, but it should. 
That's still on my TODOs.

# WARNING!
The tool does not support UNDO. Please do a backup of your projects before starting the refactoring!
