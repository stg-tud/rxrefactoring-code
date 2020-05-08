package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
import de.tudarmstadt.rxrefactoring.core.utils.RelevantInvocation;
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.WorkerUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.DiscoveringVisitor;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016<br>
 * Adapted to new core by Camila Gonzalez on 19/01/2018
 */
public class RxCollector implements IWorker<Void, RxCollector> {
	
	public RefactorScope scope;
	
	public RxCollector(RefactorScope scope) {
		this.scope = scope;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public @Nullable RxCollector refactor(@NonNull IProjectUnits units, @Nullable Void input,
			@NonNull WorkerSummary summary) throws Exception {
		String className = SwingWorkerInfo.getBinaryName();
		Set<IRewriteCompilationUnit> newUnits = Sets.newConcurrentHashSet();

		for (IRewriteCompilationUnit unit : units) {
			// Initialize Visitor
			DiscoveringVisitor discoveringVisitor = new DiscoveringVisitor(className);
			// Collect information using visitor
			unit.setWorkerIdentifier(new WorkerIdentifier("All changes for " + unit.getElementName()));
			unit.accept(discoveringVisitor);

			if (scope.equals(RefactorScope.WHOLE_PROJECT)){
				WorkerUtils.fillAllMap();
				for(Entry<WorkerIdentifier, Multimap<IRewriteCompilationUnit, ?>> m : WorkerUtils.getAllMap().entries()) {		
					m.getValue().putAll(unit, WorkerUtils.getNeededList(m.getKey(), discoveringVisitor));
				}
			} else if (scope.equals(RefactorScope.SEPARATE_OCCURENCES) || scope.equals(RefactorScope.ONLY_ONE_OCCURENCE)) {
				Set<IRewriteCompilationUnit> allWorkerUnits = loopOverEveryWorker(unit, discoveringVisitor);
				newUnits.addAll(allWorkerUnits);
				units.remove(unit);
			}
			
			discoveringVisitor.cleanAllLists();

		}
		
		WorkerUtils.clearKeys();

		units.addAll(newUnits);

		return this;
	}

	
	public Set<IRewriteCompilationUnit> loopOverEveryWorker( IRewriteCompilationUnit unit, DiscoveringVisitor visitor) {
		Set<IRewriteCompilationUnit> allWorkerUnits = Sets.newConcurrentHashSet();

		for(WorkerIdentifier identifier: WorkerUtils.getAllIdentifier()) {
			
			for (Object m : WorkerUtils.getNeededList(identifier, visitor)) {
				ASTNode newNode = unit.copyNode(unit.getRoot());
				RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
				newUnit.setWorkerIdentifier(identifier);
				WorkerUtils.addElementToList(identifier, visitor, newUnit, m);
				allWorkerUnits.add(newUnit);
			}
		}
		
		return allWorkerUnits;
	}
	


	public Multimap<IRewriteCompilationUnit, TypeDeclaration> getTypeDeclMap() {
		return WorkerUtils.getTypeDeclMap();
	}

	public Multimap<IRewriteCompilationUnit, FieldDeclaration> getFieldDeclMap() {
		return WorkerUtils.getFieldDeclMap();
	}

	public Multimap<IRewriteCompilationUnit, Assignment> getAssigmentsMap() {
		return WorkerUtils.getAssigmentsMap();
	}

	public Multimap<IRewriteCompilationUnit, VariableDeclarationStatement> getVarDeclMap() {
		return WorkerUtils.getVarDeclMap();
	}

	public Multimap<IRewriteCompilationUnit, SimpleName> getSimpleNamesMap() {
		return WorkerUtils.getSimpleNamesMap();
	}

	public Multimap<IRewriteCompilationUnit, ClassInstanceCreation> getClassInstanceMap() {
		return WorkerUtils.getClassInstanceMap();
	}

	public Multimap<IRewriteCompilationUnit, SingleVariableDeclaration> getSingleVarDeclMap() {
		return WorkerUtils.getSingleVarDeclMap();
	}

	public static Multimap<IRewriteCompilationUnit, MethodInvocation> getMethodInvocationsMap() {
		return WorkerUtils.getMethodInvocationsMap();
	}

	public Multimap<IRewriteCompilationUnit, MethodDeclaration> getMethodDeclarationsMap() {
		return WorkerUtils.getMethodDeclarationsMap();
	}

	public Multimap<IRewriteCompilationUnit, MethodInvocation> getRelevantInvocations() {
		return WorkerUtils.getRelevantInvocations();
	}

}
