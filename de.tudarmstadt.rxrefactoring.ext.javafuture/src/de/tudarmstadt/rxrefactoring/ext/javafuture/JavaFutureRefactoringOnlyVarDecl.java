package de.tudarmstadt.rxrefactoring.ext.javafuture;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;
import de.tudarmstadt.rxrefactoring.core.utils.NamingUtils;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
import de.tudarmstadt.rxrefactoring.ext.javafuture.analysis.PreconditionWorker;
import de.tudarmstadt.rxrefactoring.ext.javafuture.analysis.UseDefWorker;
import de.tudarmstadt.rxrefactoring.ext.javafuture.dependencies.CursorRefactorOccurenceSearcher;
import de.tudarmstadt.rxrefactoring.ext.javafuture.dependencies.DependencyCheckerJavaFuture;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfos;
import de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation.InstantiationCollector;
import de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation.SubclassInstantiationCollector;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.WorkerUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.FutureCollector;


public class JavaFutureRefactoringOnlyVarDecl extends JavaFutureRefactoring {
	
	private EnumSet<RefactoringOptions> options;
	private FutureCollector futureCollector;

	public JavaFutureRefactoringOnlyVarDecl() {
		options = EnumSet.of(RefactoringOptions.FUTURE, RefactoringOptions.ONLY_ONE_OCCURENCY);// , RefactoringOptions.FUTURETASK);
		futureCollector = new FutureCollector(options);
	}
	
	@Override
	public void setRefactorScope(RefactorScope scope) {
		if(scope.equals(RefactorScope.ONLY_ONE_OCCURENCE))
				options.add(RefactoringOptions.ONLY_ONE_OCCURENCY);
		
	}
	
	@Override
	public RefactorScope getRefactorScope() {
		if(options.contains(RefactoringOptions.ONLY_ONE_OCCURENCY))
			return RefactorScope.ONLY_ONE_OCCURENCE;
		
		return RefactorScope.NO_SCOPE;
	}
	
	@Override
	public ProjectUnits runDependencyBetweenWorkerCheck(ProjectUnits units, MethodScanner scanner, int startLine) throws JavaModelException{
		DependencyCheckerJavaFuture dependencyCheck = new DependencyCheckerJavaFuture(units, scanner, futureCollector, startLine);
		return dependencyCheck.runDependendencyCheck(true);
	}
	
	@Override
	public ProjectUnits analyseCursorPosition(ProjectUnits units, int startLine) {
		CursorRefactorOccurenceSearcher searcher = new CursorRefactorOccurenceSearcher(units, startLine, futureCollector);
		return searcher.searchOccurence();
		
	}
	
	@Override
	public @NonNull String getName() {
		return "Java Future to Observable only Variable Declarations";
	}
	
	@Override
	public String getDescription() {
		return "Only Variable Declaration";
	}
	
	@Override
	public boolean hasInteractiveRefactorScope() {
		return false;
	}
	@Override
	public boolean onlyScanOpenFile() {
		return true;
	}
	

	@Override
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		IWorkerRef<Void, Map<ASTNode, UseDef>> analysisRef = workerTree.addWorker(new UseDefWorker());
		
		IWorkerRef<Map<ASTNode, UseDef>, InstantiationCollector> instRef = workerTree.addWorker(analysisRef,
				new InstantiationCollector(ClassInfos.Future));
		IWorkerRef<InstantiationCollector, SubclassInstantiationCollector> subclassInstRef = workerTree
				.addWorker(instRef, new SubclassInstantiationCollector());
		IWorkerRef<SubclassInstantiationCollector, PreconditionWorker> instUseRef = workerTree
				.addWorker(subclassInstRef, new PreconditionWorker());

		IWorkerRef<PreconditionWorker, FutureCollector> collector = workerTree.addWorker(instUseRef,
				futureCollector);
		
		if(options.contains(RefactoringOptions.FUTURE)) {
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.VariableDeclStatementWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.AssignmentWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.MethodInvocationWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.future.SingleVariableDeclWorker());
			
			
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.VariableDeclStatementWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.AssignmentWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.MethodInvocationWorker());
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.ClassInstanceCreationWorker());
			
			WorkerUtils.addIdentifierToAll(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER);
			WorkerUtils.addIdentifierToAll(NamingUtils.ASSIGNMENTS_IDENTIFIER);
			WorkerUtils.addIdentifierToAll(NamingUtils.METHOD_INVOCATION_IDENTIFIER);
			WorkerUtils.addIdentifierToAll(NamingUtils.SINGLE_VAR_DECL_IDENTIFIER);
			WorkerUtils.addIdentifierToAll(NamingUtils.CLASS_INSTANCE_CREATION_IDENTIFIER);
		}
		if (options.contains(RefactoringOptions.FUTURETASK)) {
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.VariableDeclStatementWorker());
			WorkerUtils.addIdentifierToAll(NamingUtils.VAR_DECL_STATEMENT_IDENTIFIER);
		}
		
	}
}
