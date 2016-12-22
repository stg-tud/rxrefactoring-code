package rxjavarefactoring;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import domain.ClassDetails;
import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.visitors.GeneralVisitor;
import rxjavarefactoring.processor.ASTNodesCollector;
import workers.AnonymClassWorker;

public class SwingWorkerExtension implements RxJavaRefactoringExtension<ASTNodesCollector>
{

	private static final String SWING_WORKER_ID = "rxRefactoring.commands.rxJavaRefactoringSwingWorker";
	private static final String COLLECTOR_NAME = "SwingWorker";

	@Override
	public String getId()
	{
		return SWING_WORKER_ID;
	}

	@Override
	public ASTNodesCollector getASTNodesCollectorInstance()
	{
		return new ASTNodesCollector( COLLECTOR_NAME );
	}

	@Override
	public void processUnit( ICompilationUnit unit, ASTNodesCollector collector )
	{

		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( unit, true );
		GeneralVisitor generalVisitor = new GeneralVisitor( ClassDetails.SWING_WORKER.getBinaryName() );

		cu.accept( generalVisitor );

		// Cache relevant information in an object that contains maps
		collector.addSubclasses( unit, generalVisitor.getSubclasses() );
		collector.addAnonymClassDecl( unit, generalVisitor.getAnonymousClasses() );
		collector.addVariableDeclarations( unit, generalVisitor.getVariableDeclarations() );
		collector.addMethodInvocatons( unit, generalVisitor.getMethodInvocations() );
	}

	@Override
	public Set<AbstractRefactorWorker<ASTNodesCollector>> getRefactoringWorkers( ASTNodesCollector collector )
	{
		Set<AbstractRefactorWorker<ASTNodesCollector>> workers = new HashSet<>();
		workers.add( new AnonymClassWorker( collector ) );
		return workers;
	}

}
