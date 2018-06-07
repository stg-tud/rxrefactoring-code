package de.tudarmstadt.rxrefactoring.ext.javafuture.workers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.ext.javafuture.RefactoringOptions;
import de.tudarmstadt.rxrefactoring.ext.javafuture.analysis.PreconditionWorker;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfos;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.FutureCollectionVisitor2;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.FutureVisitor;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.FutureVisitor3;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class FutureCollector implements IWorker<PreconditionWorker, FutureCollector> {

	private final Map<String, CollectorGroup> groups;

	private final Map<IRewriteCompilationUnit, Map<ASTNode, MethodDeclaration>> parentMethod;
	//private final Map<IRewriteCompilationUnit, Map<MethodDeclaration, Boolean>> isMethodPure;
	public Multimap<ASTNode, MethodInvocation> collectionGetters = HashMultimap.create();
	public Multimap<MethodDeclaration, ASTNode> methodDeclarationReturns = HashMultimap.create();
	public Multimap<MethodDeclaration, ASTNode> methodDeclarationParams = HashMultimap.create();
	
	private final EnumSet<RefactoringOptions> options;

	public FutureCollector(EnumSet<RefactoringOptions> options) {
		this.options = options;

		groups = new HashMap<>();

		parentMethod = new HashMap<>();
		//isMethodPure = new HashMap<>();
	}

	@Override
	public FutureCollector refactor(IProjectUnits units, PreconditionWorker input, WorkerSummary summary) throws Exception {
		this.collectionGetters = input.collectionGetters;
		this.methodDeclarationReturns = input.methodDeclarations;
		this.methodDeclarationParams = input.methodDeclarationParams;
		
		for (IRewriteCompilationUnit unit : units) {
			// Collect the data
			if (options.contains(RefactoringOptions.FUTURE)) {
				FutureVisitor3 discoveringVisitor = new FutureVisitor3(ClassInfos.Future, input);
				FutureCollectionVisitor2 collectionDiscoveringVisitor = new FutureCollectionVisitor2(ClassInfos.Future.getBinaryName(), input);

				unit.accept(discoveringVisitor);

				add("future", unit, discoveringVisitor);
				
				//TODO remove for now
				unit.accept(collectionDiscoveringVisitor);
				add("collection", unit, collectionDiscoveringVisitor);
			}

			if (options.contains(RefactoringOptions.FUTURETASK)) {
				FutureVisitor futureTaskDiscoveringVisitor = new FutureVisitor(ClassInfos.FutureTask);
				unit.accept(futureTaskDiscoveringVisitor);
				add("futuretask", unit, futureTaskDiscoveringVisitor);
			}
		}

		summary.setCorrect("numberOfCompilationUnits", units.size());

		return this;
	}

	public void add(String group, IRewriteCompilationUnit cu, VisitorNodes subclasses) {

		// Create group if it doesn't exist yet
		if (!groups.containsKey(group))
			groups.put(group, new CollectorGroup());

		CollectorGroup collectorGroup = groups.get(group);

		collectorGroup.add(cu, subclasses);

		//addPureInformation(cu, subclasses.getParentMethods(), subclasses.getIsMethodPures());
	}

	/*
	private void addPureInformation(IRewriteCompilationUnit cu, Map<ASTNode, MethodDeclaration> parentMethods,
			Map<MethodDeclaration, Boolean> isMethodPures) {

		addParentMethods(cu, parentMethods);
		addIsPureInfo(cu, isMethodPures);
	}

	private void addParentMethods(IRewriteCompilationUnit cu, Map<ASTNode, MethodDeclaration> parentMethods) {
		if (parentMethods == null || parentMethods.isEmpty()) {
			return;
		}

		Map<ASTNode, MethodDeclaration> currentMap = parentMethod.get(cu);
		if (currentMap == null) {
			parentMethod.put(cu, parentMethods);
		} else {
			currentMap.putAll(parentMethods);
		}
	}
	private void addIsPureInfo(IRewriteCompilationUnit cu, Map<MethodDeclaration, Boolean> isMethodPures) {
		if (isMethodPures == null || isMethodPures.isEmpty()) {
			return;
		}

		Map<MethodDeclaration, Boolean> currentMap = isMethodPure.get(cu);
		if (currentMap == null) {
			isMethodPure.put(cu, isMethodPures);
		} else {

			for (Map.Entry<MethodDeclaration, Boolean> entry : isMethodPures.entrySet()) {
				// If value already exists, only change it if the current value is true.
				if (currentMap.containsKey(entry.getKey())) {

					if (currentMap.get(entry.getKey())) {
						currentMap.put(entry.getKey(), entry.getValue());
					}
				} else {
					currentMap.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}
	*/
	
	public Collection<ASTNode> refactorParameter(MethodDeclaration md) {
		if (methodDeclarationParams.containsKey(md)) {
			return methodDeclarationParams.get(md);
		}
		return null;
	}
	
	public boolean refactorReturnStatement(MethodDeclaration md) {
		return methodDeclarationReturns.containsKey(md);
	}

	// TODO helper class is not used
	public boolean isPure(IRewriteCompilationUnit cu, ASTNode node) {
		//return isMethodPure.get(cu).getOrDefault(getParentMethod(cu, node), false);
		return true;
	}

	public MethodDeclaration getParentMethod(IRewriteCompilationUnit cu, ASTNode node) {
		return parentMethod.get(cu).getOrDefault(node, null);
	}

	public boolean containsMethodDeclaration(String group, IMethodBinding methodBinding) {

		if (methodBinding == null)
			return false;

		for (Map.Entry<IRewriteCompilationUnit, List<MethodDeclaration>> methodDeclEntry : getMethodDeclarationsMap(
				group).entrySet()) {
			for (MethodDeclaration methodDeclaration : methodDeclEntry.getValue()) {
				
				IMethodBinding decBinding = methodDeclaration.resolveBinding();
				
				if (methodBinding.toString().equals(decBinding.toString()) &&
						methodBinding.getDeclaringClass().getBinaryName().equals(decBinding.getDeclaringClass().getBinaryName())
						)
					return true;
			}
		}

		return false;
	}

	public Map<IRewriteCompilationUnit, List<TypeDeclaration>> getTypeDeclMap(String group1, String group2) {
		return merge(groups.get(group1).getTypeDeclMap(), groups.get(group2).getTypeDeclMap());
	}

	public Map<IRewriteCompilationUnit, List<TypeDeclaration>> getTypeDeclMap(String group) {
		return groups.get(group).getTypeDeclMap();
	}

	public Map<IRewriteCompilationUnit, List<FieldDeclaration>> getFieldDeclMap(String group) {
		return groups.get(group).getFieldDeclMap();
	}

	public Map<IRewriteCompilationUnit, List<Assignment>> getAssigmentsMap(String group) {
		return groups.get(group).getAssigmentsMap();
	}

	public Map<IRewriteCompilationUnit, List<VariableDeclarationStatement>> getVarDeclMap(String group) {
		return groups.get(group).getVarDeclMap();
	}

	public Map<IRewriteCompilationUnit, List<SimpleName>> getSimpleNamesMap(String group) {
		return groups.get(group).getSimpleNamesMap();
	}

	public Map<IRewriteCompilationUnit, List<ClassInstanceCreation>> getClassInstanceMap(String group) {
		return groups.get(group).getClassInstanceMap();
	}

	public Map<IRewriteCompilationUnit, List<SingleVariableDeclaration>> getSingleVarDeclMap(String group) {
		return groups.get(group).getSingleVarDeclMap();
	}

	public Map<IRewriteCompilationUnit, List<MethodInvocation>> getMethodInvocationsMap(String group) {
		return groups.get(group).getMethodInvocationsMap();
	}

	public Map<IRewriteCompilationUnit, List<MethodDeclaration>> getMethodDeclarationsMap(String group) {
		return groups.get(group).getMethodDeclarationsMap();
	}

	public Map<IRewriteCompilationUnit, List<ArrayCreation>> getArrayCreationsMap(String group) {
		return groups.get(group).getArrayCreationsMap();
	}

	public Map<IRewriteCompilationUnit, List<ReturnStatement>> getReturnStatementsMap(String group) {
		return groups.get(group).getReturnStatementsMap();
	}

	/**
	 * If it is necessary to loop over multiple groups, this method can combine the
	 * wanted maps. E. g. collector.merge(collector.getVarDeclMap("future"),
	 * collector.getVarDeclMap("collection"));
	 * 
	 * Note: More like a workaround.
	 * 
	 * @param maps
	 * @return
	 */
	@SafeVarargs
	public final <T> Map<IRewriteCompilationUnit, List<T>> merge(Map<IRewriteCompilationUnit, List<T>>... maps) {
		if (maps.length == 0)
			return null;
		if (maps.length == 1)
			return maps[0];

		Map<IRewriteCompilationUnit, List<T>> result = new HashMap<>(maps[0]);

		for (int i = 1; i < maps.length; i++) {
			for (Map.Entry<IRewriteCompilationUnit, List<T>> entry : maps[i].entrySet()) {
				result.merge(entry.getKey(), entry.getValue(), (a, b) -> {
					List<T> combined = new ArrayList<T>(a);
					combined.addAll(b);
					return combined;
				});
			}
		}

		return result;
	}

	public int getNumberOfCompilationUnits() {

		Set<IRewriteCompilationUnit> allCompilationUnits = new HashSet<>();

		for (CollectorGroup group : groups.values()) {
			allCompilationUnits.addAll(group.getCompilationUnits());
		}

		return allCompilationUnits.size();
	}

	public Map<String, Object> getResults() {
		HashMap<String, Object> map = new HashMap<>();

		for (CollectorGroup group : groups.values()) {
			Map<String, Integer> results = group.getMapsResults();

			for (Map.Entry<String, Integer> entry : results.entrySet()) {
				map.merge(entry.getKey(), entry.getValue(), (a, b) -> {
					return (int) a + (int) b;
				});
			}
		}

		map.put("Files", getNumberOfCompilationUnits());

		return map;
	}

	public boolean isGetter(MethodInvocation methodInvocation) {
		return (collectionGetters.containsValue(methodInvocation));
	}

}
