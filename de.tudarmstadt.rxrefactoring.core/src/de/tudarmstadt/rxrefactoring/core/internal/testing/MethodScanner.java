package de.tudarmstadt.rxrefactoring.core.internal.testing;

import java.util.*;
import java.util.stream.Collectors;

import de.tudarmstadt.rxrefactoring.core.utils.Log;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;

/**
 * The method scanner finds methods that are directly impacted by the
 * refactoring, as well as ones that call the impacted methods.
 * 
 * @author Nikolas Hanstein, Maximilian Kirschner
 */
class MethodScanner {
	/**
	 * The string used for signatures that could not be created due to errors.
	 */
	public static final String MISSING_SIGNATURE = "Hier könnte Ihre Werbung stehen!";
	
	
	private Map<MethodDeclaration, IRewriteCompilationUnit> refactoredMethods = Maps.newHashMap();
	private Map<MethodDeclaration, IRewriteCompilationUnit> callingMethods = Maps.newHashMap();
	

	/**
	 * Find both {@link #findImpactedMethods(ProjectUnits) impacted} and
	 * {@link #findCallingMethods(ProjectUnits) calling} methods, i.e. methods
	 * that are directly impacted by the refactoring and methods that call the
	 * impacted ones.
	 * 
	 * @param units The project compilation units to work on.
	 * @return A pair of impacted methods and calling methods.
	 */
	public void scan(ProjectUnits units) {
		Objects.requireNonNull(units);
		
		//Add impacted classes and methods
		refactoredMethods.putAll(findImpactedMethods(units));
		//Add methods that are calling impacted methods
		callingMethods.putAll(findCallingMethods(units));
		
	}
	

	public ScanResult getResult() {
		return new ScanResult();
	}


	public class ScanResult {

		private ImmutableSet<MethodDeclaration> testMethods = null;
		private ImmutableSet<TypeDeclaration> testClasses = null;


		public ImmutableSet<MethodDeclaration> getTestMethods() {
			if (testMethods != null) return testMethods;

			ImmutableSet.Builder<MethodDeclaration> builder = ImmutableSet.builder();

			builder.addAll(refactoredMethods.entrySet().stream()
				.filter(entry -> Modifier.isPublic(entry.getKey().getModifiers()))
				.filter(entry -> !methodHasChangedSignature(entry.getKey(), entry.getValue().writer()))
				.map(entry -> entry.getKey())
				.collect(Collectors.toSet())
			);

			builder.addAll(callingMethods.entrySet().stream()
				.filter(entry -> Modifier.isPublic(entry.getKey().getModifiers()))
				.filter(entry -> !methodHasChangedSignature(entry.getKey(), entry.getValue().writer()))
				.map(entry -> entry.getKey())
				.collect(Collectors.toSet())
			);

			testMethods = builder.build();
			return testMethods;
		}


		public ImmutableSet<TypeDeclaration> getTestClasses() {
			if (testClasses != null) return testClasses;

			ImmutableSet.Builder<TypeDeclaration> builder = ImmutableSet.builder();

			for (MethodDeclaration testMethod : getTestMethods()) {
				ASTNode parent = testMethod.getParent();

				if (parent instanceof TypeDeclaration)
					builder.add((TypeDeclaration) parent);
				else
					Log.error(MethodScanner.class, "cannot test refactored method of non-class [" + parent + "]:\n" + testMethod);
			}

			return builder.build();
		}

		public Set<MethodDeclaration> getOmmittedMethodsIn(TypeDeclaration decl) {
			return Arrays.stream(decl.getMethods())
				.filter(mthd -> !getTestMethods().contains(mthd))
				.collect(Collectors.toSet());
		}

		public  Set<MethodDeclaration> getOmmittedMethods() {
			return getTestClasses().stream()
				.flatMap(cls -> getOmmittedMethodsIn(cls).stream())
				.collect(Collectors.toSet());
		}
	}




	/**
	 * Finds methods that are directly impacted by the refactoring, i.e. ones that
	 * the rewriter flags as 'needing a rewrite'.
	 * 
	 * @param units The project compilation units to work on.
	 * @return A set of all impacted methods.
	 */
	private ImmutableMap<MethodDeclaration, IRewriteCompilationUnit> findImpactedMethods(ProjectUnits units) {
		ImmutableMap.Builder<MethodDeclaration, IRewriteCompilationUnit> builder = ImmutableMap.builder();
		
		for (IRewriteCompilationUnit unit : units) {
			if (unit.hasChanges()) {
								
				CompilationUnit cu = (CompilationUnit) unit.getRoot();
				for (Object objType : cu.types()) {
					// We're only interested in type declarations:
					// AnnotationTypeDeclaration can't declare regular methods
					// and EnumDeclarations are not refactored (yet)
					// TODO If EnumDeclarations ever become important, this will
					// have to be extended
					if (objType instanceof TypeDeclaration) {
						TypeDeclaration type = (TypeDeclaration) objType;
						for (MethodDeclaration method : type.getMethods()) {							
							if (ASTNodes.containsNode(method, node -> nodeHasChanges(node, unit.writer()))) 
								builder.put(method, unit);
						}
					}
				}
			}
		}
		
		return builder.build();
	}

	/**
	 * Finds methods that directly call impacted methods.
	 * 
	 * @param units           The project compilation units to work on.
	 * @return A set of all calling methods.
	 */
	private Map<MethodDeclaration, IRewriteCompilationUnit> findCallingMethods(ProjectUnits units) {
		Map<MethodDeclaration, IRewriteCompilationUnit> methods = Maps.newHashMap();

		Map<IMethodBinding, MethodDeclaration> impactedBindings = Maps.newHashMap();
		refactoredMethods.keySet().forEach(mthd -> {
			IMethodBinding binding = mthd.resolveBinding();
			if (binding != null) impactedBindings.put(binding, mthd);
		});

		for (IRewriteCompilationUnit unit : units) {
			CompilationUnit cu = (CompilationUnit) unit.getRoot();		

			cu.accept(new ASTVisitor() {
				@Override
				public boolean visit(MethodInvocation node) {
					IMethodBinding binding = node.resolveMethodBinding();

					MethodDeclaration impactedMethod = impactedBindings.get(binding);
					if (impactedMethod != null)	{
						Optional<MethodDeclaration> parentMthd = ASTNodes.findParent(node, MethodDeclaration.class);
						parentMthd.ifPresent(mthd -> methods.put(mthd, unit));

					}
					return true;
				}
			});
		}

		//We can not use immutablemap builders here because methods can potenially be added more than once (if they appear twice in a project)
		return Collections.unmodifiableMap(methods);
	}


	private static boolean methodHasChangedSignature(MethodDeclaration method, ASTRewrite rewriter) {
		
		java.util.function.Function<ASTNode, Boolean> hasChanges = node -> nodeHasChanges(node, rewriter);

		return ASTNodes.containsNode(method.getName(), hasChanges)
		|| ASTNodes.containsNode(method.getReturnType2(), hasChanges)
		|| method.parameters().stream().anyMatch(par -> ASTNodes.containsNode((ASTNode) par, hasChanges));
	}

	/**
	 * Determines whether or not the specified AST node has pending changes
	 * according to the compilation unit's {@link IRewriteCompilationUnit#writer()
	 * rewriter}.<br>
	 * <br>
	 * This is basically a version of
	 * {@link IRewriteCompilationUnit#getRewrittenNode(ASTNode)} with a few fixed
	 * bugs.
	 * 
	 * @param node The node to check for pending changes.
//	 * @param root The root compilation unit for this node.
	 * @return {@code true} if the specified node has pending changes.
	 */
	@SuppressWarnings("unchecked")
	private static boolean nodeHasChanges(ASTNode node, ASTRewrite rewriter) {

		StructuralPropertyDescriptor spd = node.getLocationInParent();
		if (spd.isChildListProperty()) {
			ListRewrite lw = rewriter.getListRewrite(node.getParent(),
					(ChildListPropertyDescriptor) node.getLocationInParent());
			List<Object> rewritten = lw.getRewrittenList();
			List<Object> original = lw.getOriginalList();

			for (int i = 0; i < original.size(); i++) {
				if (Objects.equals(original.get(i), node)) {
					try {
						return rewritten.get(i) != null && rewritten.get(i) != original.get(i);
					} catch (IndexOutOfBoundsException e) {
						return false;
					}
				}
			}
		} else {
			if (rewriter.get(node.getParent(), spd) != node) {
				return true;
			}
		}
		return false;
	}
}
