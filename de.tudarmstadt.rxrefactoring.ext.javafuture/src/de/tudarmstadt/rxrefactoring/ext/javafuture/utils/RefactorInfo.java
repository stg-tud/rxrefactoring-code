package de.tudarmstadt.rxrefactoring.ext.javafuture.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import com.google.common.collect.Lists;

public class RefactorInfo {
	
		
	List<IVariableBinding> notRefactored = Lists.newLinkedList();
	
	public boolean shouldBeRefactored(ASTNode node) {
		return true;
	}
	
	class ClassInfo {
		List<IMethodBinding> nonRefactoredMethods = Lists.newLinkedList();
		List<IVariableBinding> nonRefactoredFields = Lists.newLinkedList();
		
		Map<IMethodBinding, MethodInfo> methodInfos = new HashMap<>();
	}
	
	class MethodInfo {
		List<IVariableBinding> nonRefactoredVariables = Lists.newLinkedList();
	}
	/*
	public static RefactorInfo parseUnit(IRewriteCompilationUnit unit, InstantiationUseWorker worker) {
		final RefactorInfo result = new RefactorInfo();
		
		worker.unit.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration md) {
				
			}
		});
	}
	*/

}
