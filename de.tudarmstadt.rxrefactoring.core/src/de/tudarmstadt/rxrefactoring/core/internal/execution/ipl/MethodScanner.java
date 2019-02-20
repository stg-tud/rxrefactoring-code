package de.tudarmstadt.rxrefactoring.core.internal.execution.ipl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
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
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.collect.ImmutablePair;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ipl.collect.Pair;

/**
 * The method scanner finds methods that are directly impacted by the
 * refactoring, as well as ones that call the impacted methods.
 * @author Nikolas Hanstein, Maximilian Kirschner
 */
public class MethodScanner
{
    /**
     * The string used for signatures that could not be created due to errors.
     */
    public static final String MISSING_SIGNATURE = "Hier könnte Ihre Werbung stehen!";

    /**
     * Find both {@link #findImpactedMethods(ProjectUnits) impacted} and
     * {@link #findCallingMethods(ProjectUnits, Set) calling} methods, i.e.
     * methods that are directly impacted by the refactoring and methods that
     * call the impacted ones.
     * @param units The project compilation units to work on.
     * @return A pair of impacted methods and calling methods.
     */
    public static Pair<Set<String>, Set<String>> findMethods(ProjectUnits units)
    {
        Set<String> impacted = findImpactedMethods(units);
        Set<String> calling = findCallingMethods(units, impacted);
        // For debugging only
        //Log.info(MethodScanner.class, "Impacted Methods: " + impacted);
        //Log.info(MethodScanner.class, "Calling Methods: " + calling);
        return new ImmutablePair<>(impacted, calling);
    }

    /**
     * Retains only methods whose signatures have not changed after the
     * refactoring.
     * @param impactedMethods The set of impacted methods to investigate.
     * @param units The affected project's units.
     * @return A subset of {@code impactedMethods}, containing only those
     *         impacted methods whose signature did not change.
     */
    public static Set<String> retainUnchangedMethods(Set<String> impactedMethods, ProjectUnits units)
    {
        // Optimization: Look only at classes that contain impacted methods
        // @formatter:off
        Set<String> impactedClasses = impactedMethods.stream()
                                                     .map(MethodScanner::extractClassName)
                                                     .collect(Collectors.toSet());

        Set<String> ret = new HashSet<>();
        JavaVisitor visitor = new JavaVisitor(node -> node instanceof MethodDeclaration &&
                                                      impactedMethods.contains(buildSignatureForDeclaration((MethodDeclaration)node)));
        // @formatter:on
        for(IRewriteCompilationUnit unit : units)
        {
            CompilationUnit cu = (CompilationUnit)unit.getRoot();
            for(Object objType : cu.types())
            {
                TypeDeclaration type = (TypeDeclaration)objType;
                if(impactedClasses.contains(type.resolveBinding().getBinaryName()))
                {
                    // @formatter:off
                    ret.addAll(visitor.visitCompilationUnit(cu).stream()
                                                               .map(node -> (MethodDeclaration)node)
                                                               .map(MethodScanner::buildSignatureForDeclaration)
                                                               .collect(Collectors.toSet()));
                    // @formatter:on
                }
            }
        }
        return ret;
    }

    private static String extractClassName(String signature)
    {
        String classAndMethod = signature.substring(0, signature.indexOf('('));
        return classAndMethod.substring(0, classAndMethod.lastIndexOf('.'));
    }

    /**
     * Finds methods that are directly impacted by the refactoring, i.e. ones
     * that the rewriter flags as 'needing a rewrite'.
     * @param units The project compilation units to work on.
     * @return A set of all impacted methods.
     */
    private static Set<String> findImpactedMethods(ProjectUnits units)
    {
        Set<String> ret = new HashSet<>();
        for(IRewriteCompilationUnit unit : units)
        {
            if(unit.hasChanges())
            {
                CompilationUnit cu = (CompilationUnit)unit.getRoot();
                for(Object objType : cu.types())
                {
                    TypeDeclaration type = (TypeDeclaration)objType;
                    for(MethodDeclaration method : type.getMethods())
                    {
                        // Find all nodes that have changes, and build signatures for them
                        List<ASTNode> nodes = new JavaVisitor(node -> nodeHasChanges(node, unit)).visitMethodDeclaration(method);
                        if(!nodes.isEmpty())
                        {
                            ret.add(buildSignatureForDeclaration(method));
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Finds methods that directly call impacted methods.
     * @param units The project compilation units to work on.
     * @param impactedMethods The methods that have been impacted by the
     *        refactoring.
     * @return A set of all calling methods.
     */
    private static Set<String> findCallingMethods(ProjectUnits units, Set<String> impactedMethods)
    {
        Set<String> ret = new HashSet<>();
        JavaVisitor visitor = new JavaVisitor(node -> impactedMethods.contains(buildSignatureForCalled(node)));
        for(IRewriteCompilationUnit unit : units)
        {
            CompilationUnit cu = (CompilationUnit)unit.getRoot();
            List<ASTNode> nodes = visitor.visitCompilationUnit(cu);
            // @formatter:off
            ret.addAll(nodes.stream()
                            .map(MethodScanner::buildSignatureForCallee)
                            .collect(Collectors.toList()));
            // @formatter:on
        }

        // No need to keep nulls, they indicate that the
        // method binding could not be resolved. We should
        // also remove calling methods that are also impacted
        // methods, since impacted methods will have to be
        // treated differently when generating tests.
        ret.removeIf(sig -> sig == null || impactedMethods.contains(sig));
        return ret;
    }

    /**
     * Builds a method signature for the method called by the specified AST
     * node. Note that this only works for AST nodes of the type
     * {@link MethodInvocation} or {@link MethodReference}. See
     * {@link #buildSignature(String, String, String, String)} for the format of
     * these signatures.
     * @param node The node to build a signature for.
     * @return A string representing the built signature.
     */
    private static String buildSignatureForCalled(ASTNode node)
    {
        IMethodBinding binding;
        if(node instanceof MethodInvocation)
        {
            MethodInvocation invocation = (MethodInvocation)node;
            binding = invocation.resolveMethodBinding();
        }
        else if(node instanceof MethodReference)
        {
            MethodReference reference = (MethodReference)node;
            binding = reference.resolveMethodBinding();
        }
        else
        {
            return null;
        }

        // Occurs if the binding cannot be resolved
        if(binding == null)
        {
            return null;
        }
        binding = binding.getMethodDeclaration();

        String className = binding.getDeclaringClass().getBinaryName();
        String methodName = binding.getName();
        String params = Arrays.stream(binding.getParameterTypes()).map(t -> t.getBinaryName()).collect(Collectors.joining(", "));
        String returnName = binding.getReturnType().getBinaryName();
        return buildSignature(className, methodName, params, returnName);
    }

    /**
     * Builds a method signature for the method declaration that contains the
     * specified AST node. Note that this only works for AST nodes of the type
     * {@link MethodInvocation} or {@link MethodReference}. See
     * {@link #buildSignature(String, String, String, String)} for the format of
     * these signatures.
     * @param node The node to build a signature for.
     * @return A string representing the built signature.
     */
    private static String buildSignatureForCallee(ASTNode node)
    {
        ASTNode parent = node;
        while(!(parent instanceof MethodDeclaration))
        {
            if(parent == null)
            {
                return MISSING_SIGNATURE;
            }
            parent = parent.getParent();
        }
        return buildSignatureForDeclaration((MethodDeclaration)parent);
    }

    /**
     * Builds a method signature for the specified method declaration. See
     * {@link #buildSignature(String, String, String, String)} for the format of
     * these signatures.
     * @param method The method declaration to build a signature for.
     * @return A string representing the built signature.
     */
    private static String buildSignatureForDeclaration(MethodDeclaration method)
    {
        ITypeBinding classBinding;
        ASTNode parent = method.getParent();
        if(parent instanceof AnnotationTypeDeclaration)
        {
            classBinding = ((AnnotationTypeDeclaration)parent).resolveBinding();
        }
        else if(parent instanceof AnonymousClassDeclaration)
        {
            classBinding = ((AnonymousClassDeclaration)parent).resolveBinding();
        }
        else if(parent instanceof EnumDeclaration)
        {
            classBinding = ((EnumDeclaration)parent).resolveBinding();
        }
        else if(parent instanceof TypeDeclaration)
        {
            classBinding = ((TypeDeclaration)parent).resolveBinding();
        }
        else
        {
            throw new RuntimeException("Found naughty method declaration hiding somewhere it doesn't belong (" + parent.getClass().getName() + ").");
        }
        String className = classBinding.getBinaryName();

        String methodName = method.getName().getFullyQualifiedName();
        @SuppressWarnings("unchecked") List<SingleVariableDeclaration> objParams = method.parameters();
        // @formatter:off
        String params = objParams.stream().map(SingleVariableDeclaration::resolveBinding)
                .map(IVariableBinding::getType)
                .map(ITypeBinding::getBinaryName)
                .collect(Collectors.joining(", "));
        // @formatter:on
        String returnName = method.getReturnType2().resolveBinding().getBinaryName();
        return buildSignature(className, methodName, params, returnName);
    }

    /**
     * Builds a signature based on the specified method properties. These
     * signatures are of the form
     * {@code my.package.MyClass.myMethod(my.package.MyParameterClass) :
     * my.package.MyReturnTypeClass}.
     * @param className The name of the class from which the method comes,
     *        including the package.
     * @param methodName The name of the method itself.
     * @param params The names of the classes of the parameters of this method,
     *        including the packages, separated by commas and spaces.
     * @param returnName The name of the type of class which this method
     *        returns, including the package.
     * @return A signature for the specified properties.
     */
    private static String buildSignature(String className, String methodName, String params, String returnName)
    {
        return className + "." + methodName + "(" + params + ") -> " + returnName;
    }

    /**
     * Determines whether or not the specified AST node has pending changes
     * according to the compilation unit's
     * {@link IRewriteCompilationUnit#writer() rewriter}.<br>
     * <br>
     * This is basically a version of
     * {@link IRewriteCompilationUnit#getRewrittenNode(ASTNode)} with a few
     * fixed bugs.
     * @param node The node to check for pending changes.
     * @param root The root compilation unit for this node.
     * @return {@code true} if the specified node has pending changes.
     */
    @SuppressWarnings("unchecked")
    private static boolean nodeHasChanges(ASTNode node, IRewriteCompilationUnit root)
    {
        ASTRewrite rewriter = root.writer();
        StructuralPropertyDescriptor spd = node.getLocationInParent();
        if(spd.isChildListProperty())
        {
            ListRewrite lw = rewriter.getListRewrite(node.getParent(), (ChildListPropertyDescriptor)node.getLocationInParent());
            List<Object> rewritten = lw.getRewrittenList();
            List<Object> original = lw.getOriginalList();

            for(int i = 0; i < original.size(); i++)
            {
                if(Objects.equals(original.get(i), node))
                {
                    try
                    {
                        return rewritten.get(i) != null && rewritten.get(i) != original.get(i);
                    }
                    catch(IndexOutOfBoundsException e)
                    {
                        return false;
                    }
                }
            }
        }
        else
        {
            if(rewriter.get(node.getParent(), node.getLocationInParent()) != node)
            {
                return true;
            }
        }
        return false;
    }
}
