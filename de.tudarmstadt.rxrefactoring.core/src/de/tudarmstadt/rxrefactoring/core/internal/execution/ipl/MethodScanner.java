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

public class MethodScanner
{
    public static Pair<Set<String>, Set<String>> findMethods(ProjectUnits units)
    {
        Set<String> changing = findChangingMethods(units);
        Set<String> calling = findCallingMethods(units, changing);
        return new ImmutablePair<>(changing, calling);
    }

    private static Set<String> findChangingMethods(ProjectUnits units)
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
                        String className = type.resolveBinding().getBinaryName();
                        String methodName = method.getName().getFullyQualifiedName();
                        Log.info(JavaVisitor.class, "Looking for changes in method " + className + "." + methodName + "()");
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

    private static Set<String> findCallingMethods(ProjectUnits units, Set<String> changingMethods)
    {
        Set<String> ret = new HashSet<>();
        JavaVisitor visitor = new JavaVisitor(node -> changingMethods.contains(buildSignatureFromNode(node)));
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

    private static String buildSignature(String className, String methodName, String params, String returnName)
    {
        return className + "." + methodName + "(" + params + ") -> " + returnName;
    }

    // Basically a fixed version of IRewriteCompilationUnit.getRewrittenNode
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
