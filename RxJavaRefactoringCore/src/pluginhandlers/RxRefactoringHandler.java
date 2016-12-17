package pluginhandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import rxjavarefactoring.RxJavaRefactoringApp;
import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;

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
					rxJavaRefactoringApp.setExtension( (RxJavaRefactoringExtension) extension );
					rxJavaRefactoringApp.setCommandId( commandId );
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
