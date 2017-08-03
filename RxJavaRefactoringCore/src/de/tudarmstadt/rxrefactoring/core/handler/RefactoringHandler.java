package de.tudarmstadt.rxrefactoring.core.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.tudarmstadt.rxrefactoring.core.RefactoringApp;

/**
 * Description: Starts {@link RefactoringApp} when the RxJ icon is
 * clicked.<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public abstract class RefactoringHandler extends AbstractHandler {

		
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		
		RefactorExecution execution = new RefactorExecution(
				new DefaultUI(), 
				createEnvironment()
			);	
		
		execution.run();
		
		return null;
		
		
		
//		RefactoringApp rxJavaRefactoringApp = new RefactoringApp();
//
//		// Identify the extension that triggers the event
//		IExtensionRegistry registry = Platform.getExtensionRegistry();
//		String commandId = event.getCommand().getId();
//		IConfigurationElement[] config = registry.getConfigurationElementsFor(PluginConstants.EXTENSION_POINT);
//		try {
//			for (IConfigurationElement e : config) {
//				System.out.println("Evaluating extension");
//				final Object extension = e.createExecutableExtension("class");
//				if (extension instanceof RefactoringExtension) {
//					// setup extension
//					RefactoringExtension rxJavaRefactoringExtension = (RefactoringExtension) extension;
//					String currentId = rxJavaRefactoringExtension.getId();
//					if (currentId.equals(commandId)) {
//						// Check whether the extension exists.
//						Objects.requireNonNull(extension, "Extension could not be found. Is the id set correctly?");
//
//						rxJavaRefactoringApp.setExtension((RefactoringExtension) extension);
//						rxJavaRefactoringApp.setCommandId(commandId);
//						break;
//					}
//				}
//			}
//		} catch (CoreException ex) {
//			System.out.println(ex.getMessage());
//		}
//
//		try {
//			rxJavaRefactoringApp.start(null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
	}
	
	
	public abstract RefactorEnvironment createEnvironment();
}
