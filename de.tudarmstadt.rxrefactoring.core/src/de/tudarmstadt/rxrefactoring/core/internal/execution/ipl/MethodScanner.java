package de.tudarmstadt.rxrefactoring.core.internal.execution.ipl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.tudarmstadt.rxrefactoring.core.utils.Log;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.collect.ImmutablePair;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.collect.Pair;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;

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
	
	
	private Map<MethodDeclaration, IRewriteCompilationUnit> impactedMethods = Maps.newHashMap();
	private Map<MethodDeclaration, IRewriteCompilationUnit> callingMethods = Maps.newHashMap();
	
//	private Set<AbstractTypeDeclaration> impactedTypes = Sets.newHashSet();
	
	private List<ProjectUnits> scannedUnits = Lists.newLinkedList(); 
	private List<ProjectUnits> refactoredUnits = Lists.newLinkedList();
	
	
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
		
		//Add the scanned unit
		scannedUnits.add(units);
			
		//Add impacted classes and methods
		impactedMethods.putAll(findImpactedMethods(units));
		
//		impacted.stream()
//			.flatMap(md -> ASTNodes.findParent(md, AbstractTypeDeclaration.class).stream())
//			.forEach(td -> impactedTypes.add(td));
		
		//Add methods that are calling impacted methods
		callingMethods.putAll(findCallingMethods(units));
		
//		 For debugging only
		 Log.info(MethodScanner.class, "Impacted Methods: " + impactedMethods);
		 Log.info(MethodScanner.class, "Calling Methods: " + callingMethods);
	}
	
	public void addRefactoredUnit(ProjectUnits unit) {
		refactoredUnits.add(unit);
	}

//	/**
//	 * Retains only methods whose signatures have not changed after the refactoring.
//	 *
//	 * @param units           The affected project's units after the refactoring.
//	 * @return A subset of {@code impactedMethods}, containing only those impacted
//	 *         methods whose signature did not change.
//	 */
//	public void retainUnchangedMethods(ProjectUnits units) {
//		// Optimization: Look only at classes that contain impacted methods
//		
//		Builder<MethodDeclaration> builder = ImmutableSet.builder();
//		
//		JavaVisitor visitor = new JavaVisitor(node -> node instanceof MethodDeclaration && impactedMethods.contains(node));
//		
//		for (IRewriteCompilationUnit unit : units) {
//			CompilationUnit cu = (CompilationUnit) unit.getRoot();
//			
//			for (Object objType : cu.types()) {
//				AbstractTypeDeclaration type = (AbstractTypeDeclaration) objType;
//				
//				if (impactedTypes.stream().anyMatch().contains(type)) {
//					// @formatter:off
//					builder.addAll(
//							(List) visitor.visitCompilationUnit(cu)
////							visitor.visitCompilationUnit(cu).stream()
////							.map(node -> (MethodDeclaration) node)
////							.map(MethodScanner::buildSignatureForDeclaration)
////							.collect(Collectors.toSet())
//					);
//					// @formatter:on
//				}
//			}
//		}
//		
//		ImmutableSet<MethodDeclaration> retainedMethods = builder.build();
//		
//		impactedMethods.removeIf(s -> retainedMethods.contains(s));		
//	}
	
	public ImmutableSet<MethodDeclaration> getImpactedMethods() {
		ImmutableSet.Builder<MethodDeclaration> builder = ImmutableSet.builder();
		
		Set<MethodDeclaration> publicImpacted = impactedMethods.entrySet().stream()
			.filter(entry -> Modifier.isPublic(entry.getKey().getModifiers()))
			.filter(entry -> methodHasChangedSignature(entry.getKey(), entry.getValue().writer()))
			.map(entry -> entry.getKey())
			.collect(Collectors.toSet());	
		
		
		
		
		
		return builder.build();		
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
	private ImmutableMap<MethodDeclaration, IRewriteCompilationUnit> findCallingMethods(ProjectUnits units) {
		ImmutableMap.Builder<MethodDeclaration, IRewriteCompilationUnit> builder = ImmutableMap.builder();

		Map<IMethodBinding, MethodDeclaration> impactedBindings = Maps.newHashMap();
		impactedMethods.keySet().forEach(mthd -> {
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
						parentMthd.ifPresent(mthd -> builder.put(mthd, unit));
					}
					return true;
				}
			});
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
	
	private static boolean methodHasChangedSignature(MethodDeclaration method, ASTRewrite rewriter) {
		
		java.util.function.Function<ASTNode, Boolean> hasChanges = node -> nodeHasChanges(node, rewriter);
		
		boolean hasSignatureChanged = 
			ASTNodes.containsNode(method.getName(), hasChanges) 
			|| ASTNodes.containsNode(method.getReturnType2(), hasChanges)
			|| method.parameters().stream().anyMatch(par -> ASTNodes.containsNode((ASTNode) par, hasChanges));
		
		return hasSignatureChanged;
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
