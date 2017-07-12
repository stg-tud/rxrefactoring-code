package de.tudarmstadt.rxrefactoring.core.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import de.tudarmstadt.rxrefactoring.core.RxRefactoringExtension;
import de.tudarmstadt.rxrefactoring.core.RxRefactoringApp;

/**
 * Description: Starts {@link RxRefactoringApp} when the RxJ icon is
 * clicked.<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class RxRefactoringHandler extends AbstractHandler
{

	private static final String EXTENSION_ID = "de.tudarmstadt.stg.rxjava.refactoring.extensionpoint";

	@Override
	public Object execute( ExecutionEvent event ) throws ExecutionException
	{
		RxRefactoringApp rxJavaRefactoringApp = new RxRefactoringApp();

		// Identify the extension that triggers the event
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		String commandId = event.getCommand().getId();
		IConfigurationElement[] config = registry.getConfigurationElementsFor( EXTENSION_ID );
		try
		{
			for ( IConfigurationElement e : config )
			{
				System.out.println( "Evaluating extension" );
				final Object extension = e.createExecutableExtension( "class" );
				if ( extension instanceof RxRefactoringExtension )
				{
					// setup extension
					RxRefactoringExtension rxJavaRefactoringExtension = (RxRefactoringExtension) extension;
					String currentId = rxJavaRefactoringExtension.getId();
					if (currentId.equals( commandId )) 
					{
						rxJavaRefactoringApp.setExtension( (RxRefactoringExtension) extension );
						rxJavaRefactoringApp.setCommandId( commandId );
						break;
					}
				}
			}
		}
		catch ( CoreException ex )
		{
			System.out.println( ex.getMessage() );
		}

		try
		{
			rxJavaRefactoringApp.start( null );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		return null;
	}
}
