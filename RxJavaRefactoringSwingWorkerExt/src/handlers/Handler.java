package handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import pluginhandlers.RxRefactoringHandler;

public class Handler extends AbstractHandler
{

	@Override
	public Object execute( ExecutionEvent event ) throws ExecutionException
	{
		RxRefactoringHandler handler = new RxRefactoringHandler();
		handler.execute( event );
		return null;
	}

}
