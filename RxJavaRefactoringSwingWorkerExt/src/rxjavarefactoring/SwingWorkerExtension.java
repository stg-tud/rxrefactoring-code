package rxjavarefactoring;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import domain.ClassDetails;
import rxjavarefactoring.analyzers.DeclarationVisitor;
import rxjavarefactoring.analyzers.MethodInvocationVisitor;
import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.RxLogger;
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
		DeclarationVisitor declarationVisitor = new DeclarationVisitor( ClassDetails.SWING_WORKER.getBinaryName() );
		MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor(
				ClassDetails.SWING_WORKER.getPublicMethodsMap(),
				ClassDetails.SWING_WORKER.getBinaryName() );

		cu.accept( declarationVisitor );
		cu.accept(methodInvocationVisitor);

		String location = cu.getPackage().toString()
				.replaceAll( "package ", "" )
				.replaceAll( ";", "." + cu.getJavaElement().getElementName() )
				.replaceAll( "\n", "" )
				.replaceAll( "\\.java", "" );

		String className = ClassDetails.SWING_WORKER.getBinaryName();
		if ( declarationVisitor.isTargetClassFound() )
		{
			RxLogger.info( this, "METHOD=processUnit - " + className + " found in class: " + location );
		}

		if ( methodInvocationVisitor.isUsagesFound() )
		{
			RxLogger.info( this, "METHOD=processUnit - Method Invocation of " + className + " found in class: " + location );
		}

		// Cache relevant information in an object that contains maps
		collector.addSubclasses( unit, declarationVisitor.getSubclasses() );
		collector.addAnonymClassDecl( unit, declarationVisitor.getAnonymousClasses() );
		collector.addAnonymCachedClassDecl( unit, declarationVisitor.getAnonymousCachedClasses() );
		collector.addRelevantUsages( unit, methodInvocationVisitor.getUsages() );
	}

	@Override
	public Set<AbstractRefactorWorker<ASTNodesCollector>> getRefactoringWorkers( ASTNodesCollector collector )
	{
		Set<AbstractRefactorWorker<ASTNodesCollector>> workers = new HashSet<>();
		workers.add( new AnonymClassWorker( collector ) );
		return workers;
	}

}
