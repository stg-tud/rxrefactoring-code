package rxjavarefactoring;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import domain.ClassDetails;
import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import visitors.DiscoveringVisitor;
import visitors.Collector;
import workers.AnonymClassWorker;

public class SwingWorkerExtension implements RxJavaRefactoringExtension<Collector>
{

	private static final String SWING_WORKER_ID = "rxRefactoring.commands.rxJavaRefactoringSwingWorker";
	private static final String COLLECTOR_NAME = "SwingWorker";

	@Override
	public String getId()
	{
		return SWING_WORKER_ID;
	}

	@Override
	public Collector getASTNodesCollectorInstance(IProject project)
	{
		return new Collector( COLLECTOR_NAME );
	}

	@Override
	public void processUnit( ICompilationUnit unit, Collector collector )
	{

		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( unit, true );
		DiscoveringVisitor discoveringVisitor = new DiscoveringVisitor( ClassDetails.SWING_WORKER.getBinaryName() );

		cu.accept(discoveringVisitor);

		// Cache relevant information in an object that contains maps
		collector.addSubclasses( unit, discoveringVisitor.getSubclasses() );
		collector.addAnonymClassDecl( unit, discoveringVisitor.getAnonymousClasses() );
		collector.addVariableDeclarations( unit, discoveringVisitor.getVariableDeclarations() );
		collector.addMethodInvocatons( unit, discoveringVisitor.getMethodInvocations() );
	}

	@Override
	public Set<AbstractRefactorWorker<Collector>> getRefactoringWorkers(Collector collector )
	{
		Set<AbstractRefactorWorker<Collector>> workers = new HashSet<>();
		workers.add( new AnonymClassWorker( collector ) );
		return workers;
	}

}
