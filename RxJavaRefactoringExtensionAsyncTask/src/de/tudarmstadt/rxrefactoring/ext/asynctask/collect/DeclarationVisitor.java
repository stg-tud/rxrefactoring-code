package de.tudarmstadt.rxrefactoring.ext.asynctask.collect;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.ext.asynctask.domain.ClassDetails;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskASTUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

/**
 * Description: This visitor collects all class declarations and groups then
 * into 3 groups:<br>
 * <ul>
 * <li>TypeDeclarations: Classes that extend the target class</li>
 * <li>AnonymousClassDeclarations: Target classes that are instantiated without
 * assigning the object to a variable (fire and forget)</li>
 * <li>VariableDeclarations: Target classes that are assigned to a variable
 * after instantiation</li>
 * </ul>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class DeclarationVisitor extends ASTVisitor {
	private final ClassDetails targetClass;
	private final List<TypeDeclaration> subclasses;
	private final List<AnonymousClassDeclaration> anonymousClasses;
	private final List<ASTNode> anonymousCachedClasses;

	public DeclarationVisitor(ClassDetails targetClass) {
		this.targetClass = targetClass;
		subclasses = new ArrayList<>();
		anonymousClasses = new ArrayList<>();
		anonymousCachedClasses = new ArrayList<>();
	}

	@Override
	public boolean visit(TypeDeclaration node) {
	
		if (ASTUtils.isTypeOf(node, targetClass.getBinaryName())) {			
			if (!AsyncTaskASTUtils.containsForbiddenMethod(node)) {				
				subclasses.add(node);
			} else {
				Log.info(getClass(), "Could not refactor class declaration, because it contains a forbidden method.");
			}			
		}
		return true;
	}

	
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
									
		if (ASTUtils.isTypeOf(node, targetClass.getBinaryName())) {
			if (!AsyncTaskASTUtils.containsForbiddenMethod(node)) {
				
				VariableDeclaration parent = ASTUtils.findParent(node, VariableDeclaration.class);
				Expression parentExp = ASTUtils.findParent(node, Assignment.class);
				Boolean isAssign = (parentExp instanceof Assignment ? true : false);
				
				if (parent == null && !isAssign) {
					anonymousClasses.add(node);
				} else {
					anonymousCachedClasses.add(node);
				}
			} else {
				Log.info(getClass(), "Could not refactor anonymous class, because it contains a forbidden method.");
			}
		}
		return true;
	}

	/**
	 * Subclasses correspond to Java objects that extend the target class<br>
	 * Example: public class MyClass extends TargetClass { ... }
	 * 
	 * @return A type declaration of the class extending TargetClass
	 */
	public List<TypeDeclaration> getSubclasses() {
		return subclasses;
	}

	/**
	 * AnonymousClasses correspond to class instance creations without assigning
	 * the value to a variable.<br>
	 * Example: new TargetClass(){...}
	 * 
	 * @return An anonymous class declaration of TargetClass
	 */
	public List<AnonymousClassDeclaration> getAnonymousClasses() {
		return anonymousClasses;
	}

	/**
	 * AnonymousCachedClasses correspond to class instance creations that are
	 * assigned to a variable.<br>
	 * Example: target = new TargetClass(){...}
	 * 
	 * @return A Variable declaration of TargetClass
	 */
	public List<ASTNode> getAnonymousCachedClasses() {
		return anonymousCachedClasses;
	}

	public boolean isTargetClassFound() {
		return !subclasses.isEmpty() || !anonymousClasses.isEmpty() || !anonymousCachedClasses.isEmpty();
	}
}
