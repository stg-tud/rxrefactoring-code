package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.ICompilationUnit;
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
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.WorkerMapsUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.DiscoveringVisitor;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016<br>
 * Adapted to new core by Camila Gonzalez on 19/01/2018
 */
public class RxCollector implements IWorker<Void, RxCollector> {
	
	@Override
	public @Nullable RxCollector refactor(@NonNull IProjectUnits units, @Nullable Void input,
			@NonNull WorkerSummary summary, RefactorScope scope) throws Exception {
		String className = SwingWorkerInfo.getBinaryName();
		WorkerMapsUtils.fillAllWorkerMap();
		Set<IRewriteCompilationUnit> newUnits = Sets.newConcurrentHashSet();

		for (IRewriteCompilationUnit unit : units) {
			// Initialize Visitor
			DiscoveringVisitor discoveringVisitor = new DiscoveringVisitor(className);
			// Collect information using visitor
			unit.setWorker("All");
			unit.accept(discoveringVisitor);

			if (scope.equals(RefactorScope.WHOLE_PROJECT) || scope == null) {
				for (Entry<String, Multimap<IRewriteCompilationUnit, ? extends ASTNode>> entry : WorkerMapsUtils.getAllWorkerMap().entries()) {
					entry.getValue().putAll(unit, WorkerMapsUtils.getNeededList(entry.getKey(), discoveringVisitor));
				}
			} else if (scope.equals(RefactorScope.SEPARATE_OCCURENCES)) {
				Set<IRewriteCompilationUnit> allWorkerUnits = addWorkerUnitsToMaps(unit.getPrimary(),
						discoveringVisitor, unit);
				newUnits.addAll(allWorkerUnits);
			}	

		}

		units.addAll(newUnits);
		
		

		return this;
	}


	private Set<IRewriteCompilationUnit> addWorkerUnitsToMaps(ICompilationUnit compilationUnit,
			DiscoveringVisitor allVisitor, IRewriteCompilationUnit unit) {
		Set<IRewriteCompilationUnit> allWorkerUnits = Sets.newConcurrentHashSet();
		
		for (Entry<String, Multimap<IRewriteCompilationUnit, ? extends ASTNode>> entryList : WorkerMapsUtils.getAllWorkerMap().entries()) {
			if (!WorkerMapsUtils.getNeededList(entryList.getKey(), allVisitor).isEmpty()) {
				
				ASTNode newNode = unit.copyNode(unit.getRoot());
				RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
				newUnit.setWorker(entryList.getKey());
				entryList.getValue().putAll(newUnit, WorkerMapsUtils.getNeededList(entryList.getKey(), allVisitor));
				allWorkerUnits.add(newUnit);
			}
		}

		return allWorkerUnits;

	}


	/*private Set<IRewriteCompilationUnit> checkForSameMethod(IRewriteCompilationUnit unit) {
		//TODO THA entfernen wenn Organizer umgesetzt
		MethodDeclaration outerMethod = null;
		Set<IRewriteCompilationUnit> set = Sets.newConcurrentHashSet();

		for (Entry<IRewriteCompilationUnit, MethodInvocation> entry : WorkerMapsUtils.getMethodInvocationsMap().entries()) {
			MethodInvocation m = entry.getValue();
			if (m.getExpression() != null && entry.getKey().equals(unit)) {
				MethodDeclaration actualMD = ASTNodes.findParent(m.getExpression(), MethodDeclaration.class).get();
				if (!(actualMD.equals(outerMethod)) && outerMethod != null) {
					ASTNode newNode = unit.copyNode(actualMD.getParent());
					RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
					newUnit.setWorker("methodInvocationsMap");
					set.add(newUnit);
				}
				outerMethod = actualMD;
			}
		}
		return set;
	}*/
	
	public static Multimap<String, Multimap<IRewriteCompilationUnit, ? extends ASTNode>> getAllWorkerMap() {
		return WorkerMapsUtils.getAllWorkerMap();
	}

	public Multimap<IRewriteCompilationUnit, TypeDeclaration> getTypeDeclMap() {
		return WorkerMapsUtils.getTypeDeclMap();
	}

	public Multimap<IRewriteCompilationUnit, FieldDeclaration> getFieldDeclMap() {
		return WorkerMapsUtils.getFieldDeclMap();
	}

	public Multimap<IRewriteCompilationUnit, Assignment> getAssigmentsMap() {
		return WorkerMapsUtils.getAssigmentsMap();
	}

	public Multimap<IRewriteCompilationUnit, VariableDeclarationStatement> getVarDeclMap() {
		return WorkerMapsUtils.getVarDeclMap();
	}

	public Multimap<IRewriteCompilationUnit, SimpleName> getSimpleNamesMap() {
		return WorkerMapsUtils.getSimpleNamesMap();
	}

	public Multimap<IRewriteCompilationUnit, ClassInstanceCreation> getClassInstanceMap() {
		return WorkerMapsUtils.getClassInstanceMap();
	}

	public Multimap<IRewriteCompilationUnit, SingleVariableDeclaration> getSingleVarDeclMap() {
		return WorkerMapsUtils.getSingleVarDeclMap();
	}

	public static Multimap<IRewriteCompilationUnit, MethodInvocation> getMethodInvocationsMap() {
		return WorkerMapsUtils.getMethodInvocationsMap();
	}

	public Multimap<IRewriteCompilationUnit, MethodDeclaration> getMethodDeclarationsMap() {
		return WorkerMapsUtils.getMethodDeclarationsMap();
	}

	public Multimap<IRewriteCompilationUnit, MethodInvocation> getRelevantInvocations() {
		return WorkerMapsUtils.getRelevantInvocations();
	}

	@Override
	public @Nullable RxCollector refactor(IProjectUnits units, Void input, WorkerSummary summary) throws Exception {
		// TODO Auto-generated method stub
		// only needed for classes without RefactorScope implemented
		return null;
	}

}
