package de.tudarmstadt.rxrefactoring.core.internal.execution.ipl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableSet.Builder;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.collect.ImmutablePair;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.collect.Pair;

/**
 * The method scanner finds methods that are directly impacted by the
 * refactoring, as well as ones that call the impacted methods.
 * 
 * @author Nikolas Hanstein, Maximilian Kirschner
 */
public class MethodScanner {
	/**
	 * The string used for signatures that could not be created due to errors.
	 */
	public static final String MISSING_SIGNATURE = "Hier könnte Ihre Werbung stehen!";
	
	
	private Set<String> impactedMethods = Sets.newHashSet();
	private Set<String> callingMethods = Sets.newHashSet();
	
	
	/**
	 * Find both {@link #findImpactedMethods(ProjectUnits) impacted} and
	 * {@link #findCallingMethods(ProjectUnits, Set) calling} methods, i.e. methods
	 * that are directly impacted by the refactoring and methods that call the
	 * impacted ones.
	 * 
	 * @param units The project compilation units to work on.
	 * @return A pair of impacted methods and calling methods.
	 */
	public void scan(ProjectUnits units) {
			
		ImmutableSet<String> impacted = findImpactedMethods(units);
		impactedMethods.addAll(impacted);
		
		ImmutableSet<String> calling = findCallingMethods(units, impacted);
		callingMethods.addAll(calling);
		// For debugging only
		// Log.info(MethodScanner.class, "Impacted Methods: " + impacted);
		// Log.info(MethodScanner.class, "Calling Methods: " + calling);
		
	}

	/**
	 * Retains only methods whose signatures have not changed after the refactoring.
	 * 
	 * @param impactedMethods The set of impacted methods to investigate.
	 * @param units           The affected project's units.
	 * @return A subset of {@code impactedMethods}, containing only those impacted
	 *         methods whose signature did not change.
	 */
	public static Set<String> retainUnchangedMethods(Set<String> impactedMethods, ProjectUnits units) {
		// Optimization: Look only at classes that contain impacted methods
		// @formatter:off
		Set<String> impactedClasses = impactedMethods.stream().map(MethodScanner::extractClassName)
				.collect(Collectors.toSet());

		Set<String> ret = new HashSet<>();
		JavaVisitor visitor = new JavaVisitor(node -> node instanceof MethodDeclaration
				&& impactedMethods.contains(buildSignatureForDeclaration((MethodDeclaration) node)));
		// @formatter:on
		for (IRewriteCompilationUnit unit : units) {
			CompilationUnit cu = (CompilationUnit) unit.getRoot();
			for (Object objType : cu.types()) {
				AbstractTypeDeclaration type = (AbstractTypeDeclaration) objType;
				if (impactedClasses.contains(type.resolveBinding().getBinaryName())) {
					// @formatter:off
					ret.addAll(visitor.visitCompilationUnit(cu).stream().map(node -> (MethodDeclaration) node)
							.map(MethodScanner::buildSignatureForDeclaration).collect(Collectors.toSet()));
					// @formatter:on
				}
			}
		}
		return ret;
	}

	/**
	 * Extracts the class names, including the packages, from the specified
	 * signatures. See {@link #extractClassName(String)} for more information.
	 * 
	 * @param signatures The signatures to extract the class names from.
	 * @return A set containing all extracted class names.
	 */
	public static Set<String> extractClassNames(Set<String> signatures) {
		return signatures.stream().map(MethodScanner::extractClassName).collect(Collectors.toSet());
	}

	/**
	 * Finds method signatures for all methods inside classes matching the specified
	 * class signatures.
	 * 
	 * @param units           The post-refactoring project units.
	 * @param classSignatures The class signatures for which to find the method
	 *                        signatures.
	 * @return Method signatures for all methods inside classes matching the
	 *         specified class signatures.
	 */
	public static Set<String> findAllMethods(ProjectUnits units, Set<String> classSignatures) {
		Set<String> ret = new HashSet<>();
		for (IRewriteCompilationUnit unit : units) {
			CompilationUnit cu = (CompilationUnit) unit.getRoot();
			for (Object objType : cu.types()) {
				// We're only interested in type declarations:
				// AnnotationTypeDeclaration can't declare regular methods
				// and EnumDeclarations are not refactored (yet)
				// TODO If EnumDeclarations ever become important, this will
				// have to be extended
				if (objType instanceof TypeDeclaration) {
					TypeDeclaration type = (TypeDeclaration) objType;
					if (classSignatures.contains(buildSignatureForClass(type))) {
						for (MethodDeclaration decl : type.getMethods()) {
							ret.add(buildSignatureForDeclaration(decl));
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Removes all signatures of methods that are inaccessible (i.e. non-public)
	 * from the specified set of method signatures.
	 * 
	 * @param methodSet The set of method signatures to remove from.
	 * @param units     The affected project's units.
	 */
	public static void removeInaccessibleMethods(Set<String> methodSet, ProjectUnits units) {
		for (IRewriteCompilationUnit unit : units) {
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
						int mod = method.getModifiers();
						if (!Modifier.isPublic(mod)) {
							methodSet.remove(buildSignatureForDeclaration(method));
						}
					}
				}
			}
		}
	}

	/**
	 * Extracts the class name, including the package, from the specified signature.
	 * That is, if the signature is of the form
	 * {@code my.package.MyClass.myMethod(...) -> V}, then this will return
	 * {@code my.package.MyClass}.
	 * 
	 * @param signature The signature to extract the class name from.
	 * @return The class name and package extracted from the specified signature.
	 */
	private static String extractClassName(String signature) {
		String classAndMethod = signature.substring(0, signature.indexOf('('));
		return classAndMethod.substring(0, classAndMethod.lastIndexOf('.'));
	}

	/**
	 * Finds methods that are directly impacted by the refactoring, i.e. ones that
	 * the rewriter flags as 'needing a rewrite'.
	 * 
	 * @param units The project compilation units to work on.
	 * @return A set of all impacted methods.
	 */
	private static ImmutableSet<String> findImpactedMethods(ProjectUnits units) {
		Builder<String> builder = ImmutableSet.builder();
		
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
							// Find all nodes that have changes, and build
							// signatures for them
							List<ASTNode> nodes = new JavaVisitor(node -> nodeHasChanges(node, unit))
									.visitMethodDeclaration(method);
							if (!nodes.isEmpty()) {
								builder.add(buildSignatureForDeclaration(method));
							}
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
	 * @param impacted The methods that have been impacted by the
	 *                        refactoring.
	 * @return A set of all calling methods.
	 */
	private static ImmutableSet<String> findCallingMethods(ProjectUnits units, ImmutableSet<String> impacted) {
		Builder<String> builder = ImmutableSet.builder();
		
		JavaVisitor visitor = new JavaVisitor(node -> impacted.contains(buildSignatureForCalled(node)));
		for (IRewriteCompilationUnit unit : units) {
			CompilationUnit cu = (CompilationUnit) unit.getRoot();
			List<ASTNode> nodes = visitor.visitCompilationUnit(cu);
			// @formatter:off
			builder.addAll(
					nodes.stream()
					.map(MethodScanner::buildSignatureForCallee)
					.filter(sig -> sig == null || impacted.contains(sig))
					.collect(Collectors.toList())
			);
			// @formatter:on
		}

		// No need to keep nulls, they indicate that the
		// method binding could not be resolved. We should
		// also remove calling methods that are also impacted
		// methods, since impacted methods will have to be
		// treated differently when generating tests.
				
		return builder.build();
	}

	/**
	 * Builds a class signature, consisting only of class name and package, for the
	 * specified type. This method acts as if
	 * {@code extractClassName(buildSignatureForDeclaration(...))} had been called
	 * on one of the specified type's methods.
	 * 
	 * @param type The type to build a signature for.
	 * @return A string representing the built signature.
	 */
	private static String buildSignatureForClass(TypeDeclaration type) {
		return type.resolveBinding().getBinaryName();
	}

	/**
	 * Builds a method signature for the method called by the specified AST node.
	 * Note that this only works for AST nodes of the type {@link MethodInvocation}
	 * or {@link MethodReference}. See
	 * {@link #buildSignature(String, String, String, String)} for the format of
	 * these signatures.
	 * 
	 * @param node The node to build a signature for.
	 * @return A string representing the built signature.
	 */
	private static String buildSignatureForCalled(ASTNode node) {
		IMethodBinding binding;
		if (node instanceof MethodInvocation) {
			MethodInvocation invocation = (MethodInvocation) node;
			binding = invocation.resolveMethodBinding();
		} else if (node instanceof MethodReference) {
			MethodReference reference = (MethodReference) node;
			binding = reference.resolveMethodBinding();
		} else {
			return null;
		}

		// Occurs if the binding cannot be resolved
		if (binding == null) {
			return null;
		}
		binding = binding.getMethodDeclaration();

		String className = binding.getDeclaringClass().getBinaryName();
		String methodName = binding.getName();
		String params = Arrays.stream(binding.getParameterTypes()).map(t -> t.getBinaryName())
				.collect(Collectors.joining(", "));
		String returnName = binding.getReturnType().getBinaryName();
		return buildSignature(className, methodName, params, returnName);
	}

	/**
	 * Builds a method signature for the method declaration that contains the
	 * specified AST node. Note that this only works for AST nodes of the type
	 * {@link MethodInvocation} or {@link MethodReference}. See
	 * {@link #buildSignature(String, String, String, String)} for the format of
	 * these signatures.
	 * 
	 * @param node The node to build a signature for.
	 * @return A string representing the built signature.
	 */
	private static String buildSignatureForCallee(ASTNode node) {
		ASTNode parent = node;
		while (!(parent instanceof MethodDeclaration)) {
			if (parent == null) {
				return MISSING_SIGNATURE;
			}
			parent = parent.getParent();
		}
		return buildSignatureForDeclaration((MethodDeclaration) parent);
	}

	/**
	 * Builds a method signature for the specified method declaration. See
	 * {@link #buildSignature(String, String, String, String)} for the format of
	 * these signatures.
	 * 
	 * @param method The method declaration to build a signature for.
	 * @return A string representing the built signature.
	 */
	private static String buildSignatureForDeclaration(MethodDeclaration method) {
		ITypeBinding classBinding;
		ASTNode parent = method.getParent();
		if (parent instanceof AnnotationTypeDeclaration) {
			classBinding = ((AnnotationTypeDeclaration) parent).resolveBinding();
		} else if (parent instanceof AnonymousClassDeclaration) {
			classBinding = ((AnonymousClassDeclaration) parent).resolveBinding();
		} else if (parent instanceof EnumDeclaration) {
			classBinding = ((EnumDeclaration) parent).resolveBinding();
		} else if (parent instanceof TypeDeclaration) {
			classBinding = ((TypeDeclaration) parent).resolveBinding();
		} else {
			throw new RuntimeException("Found naughty method declaration hiding somewhere it doesn't belong ("
					+ parent.getClass().getName() + ").");
		}
		String className = classBinding.getBinaryName();

		String methodName = method.getName().getFullyQualifiedName();
		@SuppressWarnings("unchecked")
		List<SingleVariableDeclaration> objParams = method.parameters();
		// @formatter:off
		String params = objParams.stream().map(SingleVariableDeclaration::resolveBinding).map(IVariableBinding::getType)
				.map(ITypeBinding::getBinaryName).collect(Collectors.joining(", "));
		// @formatter:on

		Type returnType = method.getReturnType2();
		String returnName;
		if (returnType != null) {
			returnName = returnType.resolveBinding().getBinaryName();
		} else {
			// Happens for constructors, just set the return type to their own
			// type to mirror the Java bytecode names
			returnName = className;
		}
		return buildSignature(className, methodName, params, returnName);
	}

	/**
	 * Builds a signature based on the specified method properties. These signatures
	 * are of the form
	 * {@code my.package.MyClass.myMethod(my.package.MyParameterClass) :
	 * my.package.MyReturnTypeClass}.
	 * 
	 * @param className  The name of the class from which the method comes,
	 *                   including the package.
	 * @param methodName The name of the method itself.
	 * @param params     The names of the classes of the parameters of this method,
	 *                   including the packages, separated by commas and spaces.
	 * @param returnName The name of the type of class which this method returns,
	 *                   including the package.
	 * @return A signature for the specified properties.
	 */
	private static String buildSignature(String className, String methodName, String params, String returnName) {
		return className + "." + methodName + "(" + params + ") -> " + returnName;
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
	 * @param root The root compilation unit for this node.
	 * @return {@code true} if the specified node has pending changes.
	 */
	@SuppressWarnings("unchecked")
	private static boolean nodeHasChanges(ASTNode node, IRewriteCompilationUnit root) {
		ASTRewrite rewriter = root.writer();
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
			if (rewriter.get(node.getParent(), node.getLocationInParent()) != node) {
				return true;
			}
		}
		return false;
	}
}
