package de.tudarmstadt.rxrefactoring.ext.swingworker;

import java.util.Set;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.ltk.core.refactoring.CompositeChange;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IDependencyBetweenWorkerCheck;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.WorkerMapsUtils;

public class DependencyBetweenWorkerCheck implements IDependencyBetweenWorkerCheck {
	
	private ProjectUnits units;
	
	public DependencyBetweenWorkerCheck(ProjectUnits units) {
		this.units = units;
		
	}
	
	@Override
	public ProjectUnits regroupBecauseOfMethodDependencies() {
		MethodScanner scanner = new MethodScanner();
		scanner.scan(units);
		//Map<Map.Entry<MethodDeclaration, IRewriteCompilationUnit>, Map.Entry<MethodDeclaration, IRewriteCompilationUnit>> mappingMap = scanner.mappingCalledRefactoredMethods;
		
		for(Entry<MethodDeclaration, IRewriteCompilationUnit> entry: scanner.refactoredMethods.entrySet()) {
			
			searchForMethodInvocation(entry);
			
		}
		/*for(IRewriteCompilationUnit unit: units.getUnits()) {
			for(Entry<Map.Entry<MethodDeclaration, IRewriteCompilationUnit>, Map.Entry<MethodDeclaration, IRewriteCompilationUnit>> entryMap : mappingMap.entrySet()) {
				Map.Entry<MethodDeclaration, IRewriteCompilationUnit> keyRefactored = entryMap.getKey();
				IRewriteCompilationUnit unitRefactored = keyRefactored.getValue();
				Map.Entry<MethodDeclaration, IRewriteCompilationUnit> valueCalling = entryMap.getValue();
				IRewriteCompilationUnit unitCalling = valueCalling.getValue();
				int i = 1;
				if(unit.equals(unitRefactored) || unit.equals(unitCalling)) {
					unit.setWorker("test" + i);
					i++;
				}
		    }
		}*/
		
		return units;
		
	}
	
	private void searchForMethodInvocation(Map.Entry<MethodDeclaration, IRewriteCompilationUnit> entry) {
		int i = 1;
		Map<IRewriteCompilationUnit, String> toChangeWorker = new HashMap<IRewriteCompilationUnit, String>();
		for(IRewriteCompilationUnit unit: units.getUnits()) {
			if(toChangeWorker.keySet().contains(unit)) {
				unit.setWorker(toChangeWorker.get(unit));
			}
			if(unit.getWorker().equals("Method Invocation")) {
				Collection<MethodInvocation> methodInvs = WorkerMapsUtils.getMethodInvocationsMap().get(unit);
				for(MethodInvocation m: methodInvs) {
					IMethodBinding bindingInv = m.resolveMethodBinding();
					MethodDeclaration methodDecl = entry.getKey();
					IRewriteCompilationUnit unitDecl = entry.getValue();
					IMethodBinding bindingDecl = methodDecl.resolveBinding();
					if(bindingInv.equals(bindingDecl)) {
						unit.setWorker(" same" + i);
						toChangeWorker.put(unitDecl, "same" + i);				
					}
				}
			}
		}
	
	}
	
	public void checkForClassInstanceAsReturnType(IRewriteCompilationUnit unitToCheck, ClassInstanceCreation classInstance) {
		AST ast = classInstance.getAST(); 
		
	}
	
	public boolean checkMethodDependencies(MethodDeclaration unit) {
		
		
		return false;
		
	}
	
	private Set<IRewriteCompilationUnit> checkForSameMethod(IRewriteCompilationUnit unit) {

		MethodDeclaration outerMethod = null;
		Set<IRewriteCompilationUnit> set = Sets.newConcurrentHashSet();
/*
		for (Entry<IRewriteCompilationUnit, MethodInvocation> entry : methodInvocationsMap.entries()) {
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
			}*/
		//}
		return set;
	}

}


