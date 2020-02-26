package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.ltk.core.refactoring.CompositeChange;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;

public class DependencyBetweenWorkerCheck {
	
	private IRewriteCompilationUnit[] units;
	
	public DependencyBetweenWorkerCheck(IRewriteCompilationUnit[] units) {
		this.units = units;
		
	}
	
	/*public CompositeChange regroupChangesBecauseOfDependencies() {
		for(CompositeChange change: changes) {
			if(change.getName().equals("Class Instances")) {
				CompositeChange connctedChange = checkForClassInstanceAsReturnType(change);
			}
		}
		
		return null;		
	}*/
	
	public CompositeChange checkForClassInstanceAsReturnType(IRewriteCompilationUnit unitToCheck, ClassInstanceCreation classInstance) {
		AST ast = classInstance.getAST(); 
		
		return null;
	}
	
	public boolean checkMethodDependencies(MethodDeclaration unit) {
		
		
		return false;
		
	}
	
	/*private Set<IRewriteCompilationUnit> checkForSameMethod(IRewriteCompilationUnit unit) {

		MethodDeclaration outerMethod = null;
		Set<IRewriteCompilationUnit> set = Sets.newConcurrentHashSet();

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
			}
		}
		return set;
	}*/

}


