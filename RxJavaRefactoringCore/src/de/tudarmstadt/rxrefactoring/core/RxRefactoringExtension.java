package de.tudarmstadt.rxrefactoring.core;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.core.IJavaProject;

import de.tudarmstadt.rxrefactoring.core.collect.AbstractCollector;
import de.tudarmstadt.rxrefactoring.core.collect.Collector;
import de.tudarmstadt.rxrefactoring.core.workers.RxRefactorWorker;

/**
 * Description: Interface to be implemented by clients<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/17/2016
 */
public interface RxRefactoringExtension<CollectorType extends Collector> {

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
	 * @param project
	 */
	public CollectorType createCollector(IJavaProject project);

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
//	default void processUnit(ICompilationUnit unit, CollectorType collector) {
//		collector.processCompilationUnit(unit);
//	}

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
	public Iterable<RxRefactorWorker> getRefactoringWorkers(CollectorType collector);

	/**
	 * @return Command Id of the extension
	 */
	public String getId();
		
	/**
	 * Specifies which jars should be included to the refactored project.
	 * 
	 * @return Path with all jars, or null if no jars should be included.
	 */
	public default Path getLibJars() {
		return null;
	}
	
	/**
	 * Specifies where the jars should be included to the refactored project.
	 * 
	 * @return Path where the jars should be included relative to the project root.
	 */
	public default Path getLibPath() {		
		return Paths.get("./libs");
	}
}
