package rxjavarefactoring;

import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;

import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.processor.ASTNodesCollector;

public class Extension implements RxJavaRefactoringExtension<ASTNodesCollector>
{
	private static final String COMMAND_ID = "rxRefactoring.commands.rxJavaRefactoring[COMMAND_ID]";

	@Override
	public String getId()
	{
		return COMMAND_ID;
	}

	@Override
	public ASTNodesCollector getASTNodesCollectorInstance()
	{
		return null;
	}

	@Override
	public void processUnit( ICompilationUnit unit, ASTNodesCollector collector )
	{

		
	}

	@Override
	public Set<AbstractRefactorWorker<ASTNodesCollector>> getRefactoringWorkers( ASTNodesCollector collector )
	{
		return null;
	}

}
