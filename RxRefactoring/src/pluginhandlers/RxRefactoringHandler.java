package pluginhandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import rxjavarefactoring.RxJavaRefactoringApp;

/**
 * Description: Starts {@link RxJavaRefactoringApp} when the RxJ icon is
 * clicked.<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class RxRefactoringHandler extends AbstractHandler
{

	@Override
	public Object execute( ExecutionEvent event ) throws ExecutionException
	{
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked( event );
		try
		{
			new RxJavaRefactoringApp().start( null );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		// MessageDialog.openInformation(
		// window.getShell(),
		// "Rx Refactoring",
		// "Hello, Eclipse world" );
		return null;
	}
}
