package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers;

import java.util.ArrayList;
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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.workers.IWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.RefactoringOptions;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.domain.ClassInfos;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.visitors.FutureCollectionVisitor;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.visitors.FutureVisitor;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class FutureCollector implements IWorker<Void, FutureCollector> {
	
	
	private final Map<String, CollectorGroup> groups;
	
	private final Map<RewriteCompilationUnit, Map<ASTNode, MethodDeclaration>> parentMethod;
	private final Map<RewriteCompilationUnit, Map<MethodDeclaration, Boolean>> isMethodPure;

	private final EnumSet<RefactoringOptions> options;
	
	public FutureCollector(EnumSet<RefactoringOptions> options) {
		this.options = options;
		
		groups = new HashMap<>();
		
		parentMethod = new HashMap<>();
		isMethodPure = new HashMap<>();
	}
	
	@Override
	public FutureCollector refactor(ProjectUnits units, Void input, WorkerSummary summary) throws Exception {
		
		for (RewriteCompilationUnit unit : units) {
			if(options.contains(RefactoringOptions.AKKA_FUTURE)) {
				FutureVisitor discoveringVisitor = new FutureVisitor(ClassInfos.AkkaFuture);
				FutureCollectionVisitor collectionDiscoveringVisitor = new FutureCollectionVisitor(ClassInfos.AkkaFuture.getBinaryName());
				
				unit.accept(discoveringVisitor);
				unit.accept(collectionDiscoveringVisitor);
				
				add("future", unit, discoveringVisitor);
				add("collection", unit, collectionDiscoveringVisitor);					
			}			
		}	
		
		summary.setCorrect("numberOfCompilationUnits", units.size());

		return this;
	}

	public void add(String group, RewriteCompilationUnit cu, VisitorNodes subclasses) {
		
		// Create group if it doesn't exist yet
		if(!groups.containsKey(group))
			groups.put(group, new CollectorGroup());

		CollectorGroup collectorGroup = groups.get(group);
		
		collectorGroup.add(cu, subclasses);
		
		addPureInformation(cu, subclasses.getParentMethods(), subclasses.getIsMethodPures());
	}
	
	private void addPureInformation(RewriteCompilationUnit cu, Map<ASTNode, MethodDeclaration> parentMethods, Map<MethodDeclaration, Boolean> isMethodPures) {
		
		addParentMethods(cu, parentMethods);
		addIsPureInfo(cu, isMethodPures);
	}
	
	private void addParentMethods(RewriteCompilationUnit cu, Map<ASTNode, MethodDeclaration> parentMethods) {
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
	
	private void addIsPureInfo(RewriteCompilationUnit cu, Map<MethodDeclaration, Boolean> isMethodPures) {
		if (isMethodPures == null || isMethodPures.isEmpty()) {
			return;
		}
		
		Map<MethodDeclaration, Boolean> currentMap = isMethodPure.get(cu);
		if (currentMap == null) {
			isMethodPure.put(cu, isMethodPures);
		} else {
			
			for(Map.Entry<MethodDeclaration, Boolean> entry : isMethodPures.entrySet()) {
				// If value already exists, only change it if the current value is true.
				if(currentMap.containsKey(entry.getKey())) {
					
					if(currentMap.get(entry.getKey())) {
						currentMap.put(entry.getKey(), entry.getValue());
					}
				} else {
					currentMap.put(entry.getKey(), entry.getValue());
				}
			}			
		}
	}
	
	
	public boolean isPure(RewriteCompilationUnit cu, ASTNode node) {
		return isMethodPure.get(cu).getOrDefault(getParentMethod(cu, node), false);
	}
	
	
	public MethodDeclaration getParentMethod(RewriteCompilationUnit cu, ASTNode node) {
		return parentMethod.get(cu).getOrDefault(node, null);
	}

	public boolean containsMethodDeclaration(String group, IMethodBinding methodBinding) {

		if(methodBinding == null)
			return false;
		
		for (Map.Entry<RewriteCompilationUnit, List<MethodDeclaration>> methodDeclEntry : getMethodDeclarationsMap(group).entrySet())
		{
			for (MethodDeclaration methodDeclaration : methodDeclEntry.getValue())
			{
				if(methodBinding.equals(methodDeclaration.resolveBinding()))
					return true;
			}
		}

		return false;
	}
	
	public Map<RewriteCompilationUnit, List<TypeDeclaration>> getTypeDeclMap(String group1, String group2)
	{	
		return merge(groups.get(group1).getTypeDeclMap(), groups.get(group2).getTypeDeclMap());
	}

	public Map<RewriteCompilationUnit, List<TypeDeclaration>> getTypeDeclMap(String group)
	{
		return groups.get(group).getTypeDeclMap();
	}

	public Map<RewriteCompilationUnit, List<FieldDeclaration>> getFieldDeclMap(String group)
	{
		return groups.get(group).getFieldDeclMap();
	}

	public Map<RewriteCompilationUnit, List<Assignment>> getAssigmentsMap(String group)
	{
		return groups.get(group).getAssigmentsMap();
	}

	public Map<RewriteCompilationUnit, List<VariableDeclarationStatement>> getVarDeclMap(String group)
	{
		return groups.get(group).getVarDeclMap();
	}

	public Map<RewriteCompilationUnit, List<SimpleName>> getSimpleNamesMap(String group)
	{
		return groups.get(group).getSimpleNamesMap();
	}

	public Map<RewriteCompilationUnit, List<ClassInstanceCreation>> getClassInstanceMap(String group)
	{
		return groups.get(group).getClassInstanceMap();
	}

	public Map<RewriteCompilationUnit, List<SingleVariableDeclaration>> getSingleVarDeclMap(String group)
	{
		return groups.get(group).getSingleVarDeclMap();
	}

	public Map<RewriteCompilationUnit, List<MethodInvocation>> getMethodInvocationsMap(String group)
	{
		return groups.get(group).getMethodInvocationsMap();
	}

	public Map<RewriteCompilationUnit, List<MethodDeclaration>> getMethodDeclarationsMap(String group)
	{
		return groups.get(group).getMethodDeclarationsMap();
	}
	
	public Map<RewriteCompilationUnit, List<ArrayCreation>> getArrayCreationsMap(String group) {
		return groups.get(group).getArrayCreationsMap();
	}
	
	public Map<RewriteCompilationUnit, List<ReturnStatement>> getReturnStatementsMap(String group) {
		return groups.get(group).getReturnStatementsMap();
	}
		
	/**
	 * If it is necessary to loop over multiple groups, this method can combine the wanted maps.
	 * E. g. collector.merge(collector.getVarDeclMap("future"), collector.getVarDeclMap("collection"));
	 * 
	 * Note: More like a workaround.
	 * @param maps
	 * @return
	 */
	@SafeVarargs
	public final <T> Map<RewriteCompilationUnit, List<T>> merge(Map<RewriteCompilationUnit, List<T>>... maps) {
		if(maps.length == 0)
			return null;
		if(maps.length == 1)
			return maps[0];

		Map<RewriteCompilationUnit, List<T>> result = new HashMap<>(maps[0]);

		for(int i = 1; i < maps.length; i++) {
			for(Map.Entry<RewriteCompilationUnit, List<T>> entry : maps[i].entrySet()) {
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
		
		Set<RewriteCompilationUnit> allCompilationUnits = new HashSet<>();
		
		for(CollectorGroup group : groups.values()) {
			allCompilationUnits.addAll(group.getCompilationUnits());
		}
		
		return allCompilationUnits.size();
	}

	
	
	public Map<String, Object> getResults() {
		HashMap<String, Object> map = new HashMap<>();
		
		for(CollectorGroup group : groups.values()) {
			Map<String, Integer> results = group.getMapsResults();
			
			for(Map.Entry<String, Integer> entry : results.entrySet()) {
				map.merge(entry.getKey(), entry.getValue(), (a, b) -> {
					return (int)a + (int)b;
				});
			}
		}
		
		map.put("Files", getNumberOfCompilationUnits());

		return map;
	}

	

}

