# 2Rx Extension Unit-Test Project Template

## Project for UnitTests based on RxRefactoringTestsTemplate

1.) Open Eclipse and import RxRefactoringTestsTemplate:
	- File -> Import -> General\Existing Projects into Workspace

2.) Make a copy of the project in eclipse by using Ctrl+C and Ctrl+V
	- You will be asked to give the project a new name
	
3.) Open the file META-INF/MANIFEST.MF

	a) Overview Section: Change the name and the id. For Example:
		- RxRefactoringTestsTemplate -> RxRefactoringTestsSwingWorker
		
	b) Dependencies Section: Add a dependency to the extension of the plugin
	   to be tested. For example: RxJavaRefactoringExtensionSwingWorker
	   
4.) Implement tests: The plugin works only for Eclipse projects. Therefore all
classes to be tested must be contained in a project. To simplify doing this,
the RxRefactoringTestsTemplate contains an Android and a Java App under resources.
The input classes should be in one of these projects. The expected classes 
can be anywhere. See the documentation for examples.

Notice that classes to be used in the unit tests must be exported in the 
corresponding plugin. For example: In MANIFEST.MF Export-Package: rxjavarefactoring, to
have access to the class Extension from RxJavaRefactoringExtensionTemplate