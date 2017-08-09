package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.logging.Log;
import de.tudarmstadt.rxrefactoring.core.parser.BundledCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.parser.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.workers.IWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask.domain.ClassDetails;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskASTUtils;

/**
 * Description: Collects relevant information for refactoring<br>
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class AsyncTaskCollector implements IWorker<Void,AsyncTaskCollector> {

	private final Multimap<BundledCompilationUnit, TypeDeclaration> subclassesMap;
	private final Multimap<BundledCompilationUnit, AnonymousClassDeclaration> anonymousClassesMap;
	private final Multimap<BundledCompilationUnit, AnonymousClassDeclaration> anonymousCachedClassesMap;
	private final Multimap<BundledCompilationUnit, MethodInvocation> relevantUsagesMap;
	
	public AsyncTaskCollector(String collectorName) {
		subclassesMap = HashMultimap.create();
		anonymousClassesMap = HashMultimap.create();
		anonymousCachedClassesMap = HashMultimap.create();
		relevantUsagesMap = HashMultimap.create();
	}
	
	@Override
	public AsyncTaskCollector refactor(ProjectUnits units, Void input, WorkerSummary summary) throws Exception {	
		units.forEach(unit -> processBundledCompilationUnit(unit));		
		summary.setCorrect("numberOfCompilationUnits", units.size());
		return this;
	}	

	public void addSubclasses(BundledCompilationUnit cu, Iterable<TypeDeclaration> subclasses) {
		subclassesMap.putAll(cu, subclasses);
	}

	public void addAnonymClassDecl(BundledCompilationUnit cu, Iterable<AnonymousClassDeclaration> anonymDeclarations) {
		anonymousClassesMap.putAll(cu, anonymDeclarations);
	}

	public void addAnonymCachedClassDecl(BundledCompilationUnit cu, Iterable<AnonymousClassDeclaration> anonymCachedDeclarations) {
		anonymousCachedClassesMap.putAll(cu, anonymCachedDeclarations);
	}

	public void addRelevantUsages(BundledCompilationUnit cu, Iterable<MethodInvocation> usages) {
		relevantUsagesMap.putAll(cu, usages);
	}

	public Multimap<BundledCompilationUnit, TypeDeclaration> getSubclasses() {
		return subclassesMap;
	}

	public Multimap<BundledCompilationUnit, AnonymousClassDeclaration> getAnonymousClasses() {
		return anonymousClassesMap;
	}

	public Multimap<BundledCompilationUnit, AnonymousClassDeclaration> getAnonymousCachedClasses() {
		return anonymousCachedClassesMap;
	}

	public Multimap<BundledCompilationUnit, MethodInvocation> getRelevantUsages() {
		return relevantUsagesMap;
	}

	public int getNumberOfCompilationUnits() {
		Set<ICompilationUnit> allCompilationUnits = new HashSet<>();
		allCompilationUnits.addAll(subclassesMap.keySet());
		allCompilationUnits.addAll(anonymousClassesMap.keySet());
		allCompilationUnits.addAll(anonymousCachedClassesMap.keySet());
		allCompilationUnits.addAll(relevantUsagesMap.keySet());
		return allCompilationUnits.size();
	}

	private void processBundledCompilationUnit(BundledCompilationUnit unit) {
		ASTNode root = unit.getRoot();
		
		DeclarationVisitor declarationVisitor = new DeclarationVisitor(ClassDetails.ASYNC_TASK);
		UsagesVisitor usagesVisitor = new UsagesVisitor(ClassDetails.ASYNC_TASK);
		root.accept(declarationVisitor);
		root.accept(usagesVisitor);

		// Cache relevant information in an object that contains maps
		addSubclasses(unit, declarationVisitor.getSubclasses());
		addAnonymClassDecl(unit, declarationVisitor.getAnonymousClasses());
		addAnonymCachedClassDecl(unit, declarationVisitor.getAnonymousCachedClasses());
		addRelevantUsages(unit, usagesVisitor.getUsages());	
	}
	
	
	/**
	 * Description: Collects usages information for a target class<br>
	 * This class contains code from the tool
	 * <a href="http://refactoring.info/tools/asyncdroid/">AsyncDroid</a> Author:
	 * Grebiel Jose Ifill Brito<br>
	 * Created: 11/11/2016
	 */
	class UsagesVisitor extends ASTVisitor {

		private final ClassDetails targetClass;
		private final List<MethodInvocation> usages;

		public UsagesVisitor(ClassDetails targetClass) {
			this.targetClass = targetClass;
			usages = new ArrayList<>();
		}

		@Override
		public boolean visit(MethodInvocation node) {
			IMethodBinding binding = node.resolveMethodBinding();
			if (binding == null) {
				return true;
			}

			ITypeBinding declaringClass = binding.getDeclaringClass();
			if (declaringClass == null) {
				return true;
			}

			String methodName = binding.getName();

			Set<String> publicMethods = targetClass.getPublicMethodsMap().keySet();

			boolean targetClassFound = ASTUtils.isTypeOf(declaringClass, this.targetClass.getBinaryName());
			boolean targetMethodFound = publicMethods.contains(methodName);

			DeclarationVisitor dv = new DeclarationVisitor(targetClass);
			node.accept(dv);
			if (targetClassFound && targetMethodFound && dv.getAnonymousClasses().size() == 0
					&& dv.getAnonymousCachedClasses().size() == 0) {
				usages.add(node);
			}

			return true;
		}

		public List<MethodInvocation> getUsages() {
			return usages;
		}

		public boolean isUsagesFound() {
			return !usages.isEmpty();
		}
	}
	
	
	
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
	class DeclarationVisitor extends ASTVisitor {
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


	
}
