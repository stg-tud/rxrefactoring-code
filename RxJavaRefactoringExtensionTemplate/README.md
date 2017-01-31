# 2Rx Extension Template

## Create an extension for 2Rx

1. Open Eclipse and import this project:
  - File -> Import -> General\Existing Projects into Workspace
2. Make a copy of the project in eclipse by using Ctrl+C and Ctrl+V
  - You will be asked to give the project a new name
3. Open the file META-INF/MANIFEST.MF
  - Overview Section: Change the name and the id. For Example:
    - RxJavaRefactoringExtension**Template** -> RxJavaRefactoringExtension**SwingWorker**
  - plugin.xml Section:
    - Replace __[COMMAND_NAME]__ by the name that should appear in the
      RxJava Refactorings menu.
    - Replace the three occurrences of __[COMMAND_ID]__. If __[COMMAND_NAME]__
      does not have any spaces, then you could use the same name as id.
    - The "__[__" and "__]__" symbols must be removed.
4. Open the class rxjavarefactoring.Extension.java
  - Replace __[COMMAND_ID]__ by the string given in step 3.b. The "__[__" and "__]__"
    symbols must be removed here as well.
  - Implement the interface. You can use __RxJavaRefactoringExtesionSwingWorker__ 
    as guide.

## Setup Project for UnitTests based on RxRefactoringTestsTemplate

1. Open Eclipse and import RxRefactoringTestsTemplate:
 - File -> Import -> General\Existing Projects into Workspace
2. Make a copy of the project in eclipse by using Ctrl+C and Ctrl+V
 - You will be asked to give the project a new name
3. Open the file META-INF/MANIFEST.MF
 - Overview Section: Change the name and the id. For Example:
   - RxRefactoringTests**Template** -> RxRefactoringTests**SwingWorker**
 - Dependencies Section: Add a dependency to the extension of the plugin
   to be tested. For example: __RxJavaRefactoringExtensionSwingWorker__
4. Implement tests: The plugin works only for Eclipse projects. Therefore all
   classes to be tested must be contained in a project. To simplify doing this,
   the RxRefactoringTestsTemplate contains an __Android__ and a __Java App__ under __resources__.
   The input classes should be in one of these projects. The expected classes 
   can be anywhere. See the projects __RxRefactoringTestSwingWorker__
   for an example.

Notice that classes to be used in the unit tests must be exported in the 
corresponding plugin. For example: In MANIFEST.MF Export-Package: rxjavarefactoring, to
have access to the class Extension from __RxJavaRefactoringExtensionName__
