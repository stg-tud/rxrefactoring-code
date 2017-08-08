package de.tudarmstadt.rxrefactoring.ext.asynctask.collect;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import de.tudarmstadt.rxrefactoring.core.logging.Log;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.domain.ClassDetails;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskASTUtils;

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
	private final Set<TypeDeclaration> subclasses;
	private final Set<AnonymousClassDeclaration> anonymousClasses;
	private final Set<AnonymousClassDeclaration> anonymousCachedClasses;

	public DeclarationVisitor(ClassDetails targetClass) {
		this.targetClass = targetClass;
		subclasses = new HashSet<>();
		anonymousClasses = new HashSet<>();
		anonymousCachedClasses = new HashSet<>();
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		
		if (checkClass(node) && !isAbstract(node)) {
			subclasses.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {

		if (checkClass(node)) {
			VariableDeclaration parent = ASTUtils.findParent(node, VariableDeclaration.class);
			Expression parentExp = ASTUtils.findParent(node, Assignment.class);
			Boolean isAssign = (parentExp instanceof Assignment ? true : false);

			if (parent == null && !isAssign) {
				anonymousClasses.add(node);
			} else {
				anonymousCachedClasses.add(node);
			}
		}
		return true;
	}

	/**
	 * Checks whether a class is suitable for refactoring.
	 * 
	 * @param classDecl
	 *            The AST Node representing the class declaration.
	 * @return True, if the class is an AsyncTask and no method contains a call to a
	 *         forbidden method.
	 */
	private boolean checkClass(ASTNode classDecl) {

		/*
		 * Define local visitor
		 */
		class CheckClassVisitor extends ASTVisitor {
			boolean containsForbiddenMethod = false;

			public boolean visit(MethodDeclaration node) {
				Block body = node.getBody();

				if (Objects.nonNull(body)) {
					boolean r = AsyncTaskASTUtils.containsForbiddenMethod(body);

					if (r) {
						Log.info(getClass(),
								"Could not refactor anonymous class, because it contains a forbidden method.");
						containsForbiddenMethod = true;
						return false;
					}
				}
				return !containsForbiddenMethod;
			}
		}

		if (ASTUtils.isTypeOf(classDecl, targetClass.getBinaryName())) {
			CheckClassVisitor v = new CheckClassVisitor();
			classDecl.accept(v);

			return !v.containsForbiddenMethod;
		} else {
			return false;
		}
	}
	
	private boolean isAbstract(TypeDeclaration type) {
		for (Object o : type.modifiers()) {
			if (o instanceof Modifier) {
				Modifier m = (Modifier) o;
				if (m.getKeyword().equals(ModifierKeyword.ABSTRACT_KEYWORD))
					return true;
			}
		}
		return false;
	}

	/**
	 * Subclasses correspond to Java objects that extend the target class<br>
	 * Example: public class MyClass extends TargetClass { ... }
	 * 
	 * @return A type declaration of the class extending TargetClass
	 */
	public Set<TypeDeclaration> getSubclasses() {
		return subclasses;
	}

	/**
	 * AnonymousClasses correspond to class instance creations without assigning the
	 * value to a variable.<br>
	 * Example: new TargetClass(){...}
	 * 
	 * @return An anonymous class declaration of TargetClass
	 */
	public Set<AnonymousClassDeclaration> getAnonymousClasses() {
		return anonymousClasses;
	}

	/**
	 * AnonymousCachedClasses correspond to class instance creations that are
	 * assigned to a variable.<br>
	 * Example: target = new TargetClass(){...}
	 * 
	 * @return A Variable declaration of TargetClass
	 */
	public Set<AnonymousClassDeclaration> getAnonymousCachedClasses() {
		return anonymousCachedClasses;
	}

	public boolean isTargetClassFound() {
		return !subclasses.isEmpty() || !anonymousClasses.isEmpty() || !anonymousCachedClasses.isEmpty();
	}
}
