package de.tudarmstadt.refactoringrx.core.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import de.tudarmstadt.refactoringrx.core.RxJavaRefactoringApp;
import de.tudarmstadt.refactoringrx.core.RxJavaRefactoringExtension;

/**
 * Description: Starts {@link RxJavaRefactoringApp} when the RxJ icon is
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
		RxJavaRefactoringApp rxJavaRefactoringApp = new RxJavaRefactoringApp();

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
				if ( extension instanceof RxJavaRefactoringExtension )
				{
					// setup extension
					RxJavaRefactoringExtension rxJavaRefactoringExtension = (RxJavaRefactoringExtension) extension;
					String currentId = rxJavaRefactoringExtension.getId();
					if (currentId.equals( commandId )) 
					{
						rxJavaRefactoringApp.setExtension( (RxJavaRefactoringExtension) extension );
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
