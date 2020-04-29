package de.tudarmstadt.rxrefactoring.ext.javafuture;

import java.util.EnumSet;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTNode;

import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
import de.tudarmstadt.rxrefactoring.ext.javafuture.analysis.PreconditionWorker;
import de.tudarmstadt.rxrefactoring.ext.javafuture.analysis.UseDefWorker;
import de.tudarmstadt.rxrefactoring.ext.javafuture.dependencies.CursorRefactorOccurenceSearcher;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfos;
import de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation.InstantiationCollector;
import de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation.SubclassInstantiationCollector;
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
	public ProjectUnits analyseCursorPosition(ProjectUnits units, int offset, int startLine) {
		CursorRefactorOccurenceSearcher searcher = new CursorRefactorOccurenceSearcher(units, startLine, futureCollector);
		return searcher.searchOccurence();
		
	}
	
	@Override
	public @NonNull String getName() {
		return "Java Future to Observable only Variable Declarations";
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
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection.VariableDeclStatementWorker());
		}
		if (options.contains(RefactoringOptions.FUTURETASK)) {
			workerTree.addWorker(collector,
					new de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask.VariableDeclStatementWorker());
		}
		
	}
}
