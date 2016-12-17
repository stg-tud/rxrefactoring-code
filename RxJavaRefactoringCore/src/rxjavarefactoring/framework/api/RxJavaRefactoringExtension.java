package rxjavarefactoring.framework.api;

import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;

import rxjavarefactoring.framework.refactoring.AbstractCollector;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;

public interface RxJavaRefactoringExtension<CollectorType extends AbstractCollector>
{

	/**
	 * Returns the object responsible of collecting the relevant
	 * information for the refactoring task.<br>
	 *
	 * Steps:
	 * 1.) Implement a class XYZ that extends {@link AbstractCollector}
	 * before implementing this interface.<br>
	 *
	 * 2.) Return an instance of XYZ. {@code new XYZ()}
	 * 
	 * @return an instance of XYZ to collect information
	 */
	CollectorType getASTNodesCollectorInstance();

	/**
	 * This method is responsible for adding the relevant information
	 * to the collector, usually {@link ASTNode}s.<br>
	 *
	 * The method will be invoked for all compilation
	 * units in the project. At the end, the collector must contain
	 * all relevant information to perform the refactorings. Consider
	 * using List, Sets, Maps, or similar data structures to accumulate
	 * the {@link ASTNode} in the collector XYZ extends {@link AbstractCollector}.
	 *
	 * @param unit
	 *            current compilation unit
	 * @param collector
	 *            collector
	 */
	void processUnit( ICompilationUnit unit, CollectorType collector );

	/**
	 * Returns a list of workers that performs the refactorings
	 * by using the information accumulatated in the collector
	 * XYZ extends {@link AbstractCollector}.<br>
	 *
	 * Consider using a Worker for each refactoring case to avoid
	 * having complex workers. The collector should help differentiating
	 * the cases.
	 * 
	 * @return list of workers in charge of performing the refactorings
	 * @param collector
	 */
	Set<AbstractRefactorWorker<CollectorType>> getRefactoringWorkers( CollectorType collector );

	String getId();
}
