package de.tudarmstadt.rxrefactoring.core.internal.execution.ipl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
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
import de.tudarmstadt.rxrefactoring.core.utils.Log;

/**
 * The method scanner finds methods that are directly impacted by the
 * refactoring, as well as ones that call the impacted methods.
 * @author Nikolas Hanstein
 */
public class MethodScanner
{
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
        return new ImmutablePair<>(impacted, calling);
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
                        // Store the class and method name, for logging and building signatures
                        String className = type.resolveBinding().getBinaryName();
                        String methodName = method.getName().getFullyQualifiedName();
                        Log.info(JavaVisitor.class, "Looking for changes in method " + className + "." + methodName + "()");

                        // Find all nodes that have changes, and build signatures for them
                        List<ASTNode> nodes = new JavaVisitor(node -> nodeHasChanges(node, unit)).visitMethodDeclaration(method);
                        if(!nodes.isEmpty())
                        {
                            @SuppressWarnings("unchecked") List<SingleVariableDeclaration> objParams = method.parameters();
                            String params = objParams.stream().map(SingleVariableDeclaration::resolveBinding)
                                    .map(IVariableBinding::getType)
                                    .map(ITypeBinding::getBinaryName)
                                    .collect(Collectors.joining(", "));
                            ret.add(buildSignature(className, methodName, params, method.getReturnType2().resolveBinding().getBinaryName()));
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
        JavaVisitor visitor = new JavaVisitor(node -> impactedMethods.contains(buildSignatureFromNode(node)));
        for(IRewriteCompilationUnit unit : units)
        {
            CompilationUnit cu = (CompilationUnit)unit.getRoot();
            List<ASTNode> nodes = visitor.visitCompilationUnit(cu);
            ret.addAll(nodes.stream()
                            .map(MethodScanner::buildSignatureFromNode)
                            .collect(Collectors.toList()));
        }
        return ret;
    }

    /**
     * Builds a method signature for the specified AST node. Note that this only
     * works if the node is of type {@link MethodInvocation} or
     * {@link MethodReference}. See
     * {@link #buildSignature(String, String, String, String)} for the format of
     * these signatures.
     * @param node The node to build a signature for.
     * @return A string representing the built signature.
     */
    private static String buildSignatureFromNode(ASTNode node)
    {
        IMethodBinding binding;
        if(node instanceof MethodInvocation)
        {
            MethodInvocation invocation = (MethodInvocation)node;
            binding = invocation.resolveMethodBinding().getMethodDeclaration();
        }
        else if(node instanceof MethodReference)
        {
            MethodReference reference = (MethodReference)node;
            binding = reference.resolveMethodBinding().getMethodDeclaration();
        }
        else
        {
            return null;
        }

        String className = binding.getDeclaringClass().getBinaryName();
        String methodName = binding.getName();
        String params = Arrays.stream(binding.getParameterTypes()).map(t -> t.getBinaryName()).collect(Collectors.joining(", "));
        String returnName = binding.getReturnType().getBinaryName();
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
