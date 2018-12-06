package de.tudarmstadt.rxrefactoring.core.internal.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;

public class SequenceGenerator
{
    @SuppressWarnings("unchecked")
    public static void printSequences(ProjectUnits units)
    {
        for(IRewriteCompilationUnit unit : units)
        {
            if(unit.hasChanges())
            {
                CompilationUnit cu = (CompilationUnit)unit.getRoot();
                for(Object objType : cu.types())
                {
                    TypeDeclaration type = (TypeDeclaration)objType;

                    if(type.getName().getFullyQualifiedName().contains("ProblematicParameter"))
                    {
                        System.out.println("Test!");
                    }

                    for(MethodDeclaration method : type.getMethods())
                    {
                        boolean hasChanges = false;
                        List<ASTNode> nodesToCheck = visitMethodDeclaration(method);

                        for(ASTNode node : nodesToCheck)
                        {
                            if(nodeHasChanges(node, unit))
                            {
                                hasChanges = true;
                                break;
                            }
                        }

                        if(hasChanges)
                        {
                            List<SingleVariableDeclaration> objParams = method.parameters();
                            // @formatter:off
                            String params = objParams.stream().map(SingleVariableDeclaration::getType)
                                                              .map(t -> t.isParameterizedType() ? ((ParameterizedType)t).getType() : t)
                                                              .map(t -> t.resolveBinding().getPackage().getName() + "." + t.toString())
                                                              .collect(Collectors.joining(", "));
                            // @formatter:on

                            String className = cu.getPackage().getName() + "." + type.getName().getFullyQualifiedName();
                            System.err.println("method : " + className + "." + method.getName().getFullyQualifiedName() + "(" + params + ")" + " : " + className);
                        }
                    }
                }
            }
        }
    }

    private static List<ASTNode> visitArrayType(ArrayType type)
    {
        return visitType(type.getElementType());
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitBlock(Block block)
    {
        // Ignore. Caused by missing finally blocks.
        if(block == null)
        {
            return Collections.emptyList();
        }

        List<ASTNode> ret = new ArrayList<>();
        ret.add(block);

        List<Statement> statements = block.statements();
        for(Statement stmt : statements)
        {
            ret.addAll(visitStatement(stmt));
        }

        return ret;
    }

    private static List<ASTNode> visitCatchClause(CatchClause stmt)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(stmt);
        ret.addAll(visitVariableDeclaration(stmt.getException()));
        ret.addAll(visitBlock(stmt.getBody()));
        return ret;
    }

    private static List<ASTNode> visitExpression(Expression expr)
    {
        // Ignore. Caused by statements like 'return;'
        if(expr == null)
        {
            return Collections.emptyList();
        }

        // TODO Finish
        if(expr instanceof LambdaExpression)
        {
            return visitLambdaExpression((LambdaExpression)expr);
        }
        else if(expr instanceof MethodInvocation)
        {
            return visitMethodInvocation((MethodInvocation)expr);
        }
        else if(expr instanceof Name)
        {
            return visitName((Name)expr);
        }
        else if(expr instanceof NumberLiteral)
        {
            return visitNumberLiteral((NumberLiteral)expr);
        }
        else if(expr instanceof StringLiteral)
        {
            return visitStringLiteral((StringLiteral)expr);
        }
        else
        {
            return throwIncompatibleJavaException("visitExpression");
        }
    }

    private static List<ASTNode> visitExpressionStatement(ExpressionStatement stmt)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitIntersectionType(IntersectionType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);

        List<Type> types = type.types();
        for(Type t : types)
        {
            ret.addAll(visitType(t));
        }

        return ret;
    }

    private static List<ASTNode> visitLambdaBody(ASTNode body)
    {
        if(body instanceof Expression)
        {
            return visitExpression((Expression)body);
        }
        else if(body instanceof Block)
        {
            return visitBlock((Block)body);
        }
        else
        {
            return throwIncompatibleJavaException("visitLambdaBody");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitLambdaExpression(LambdaExpression expr)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(expr);

        List<VariableDeclaration> parameters = expr.parameters();
        for(VariableDeclaration parameter : parameters)
        {
            ret.addAll(visitVariableDeclaration(parameter));
        }

        ret.addAll(visitLambdaBody(expr.getBody()));

        return ret;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitMethodDeclaration(MethodDeclaration method)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(method);

        ret.addAll(visitType(method.getReturnType2()));
        ret.addAll(visitName(method.getName()));

        List<SingleVariableDeclaration> params = method.parameters();
        for(SingleVariableDeclaration param : params)
        {
            ret.addAll(visitVariableDeclaration(param));
        }

        ret.addAll(visitBlock(method.getBody()));
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitMethodInvocation(MethodInvocation expr)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(expr);

        // The object this is being called on, if any
        ret.addAll(visitExpression(expr.getExpression()));

        List<Expression> args = expr.arguments();
        for(Expression arg : args)
        {
            ret.addAll(visitExpression(arg));
        }

        // The method's name
        ret.addAll(visitName(expr.getName()));

        return ret;
    }

    private static List<ASTNode> visitName(Name name)
    {
        return Arrays.asList(name);
    }

    private static List<ASTNode> visitNumberLiteral(NumberLiteral expr)
    {
        return Arrays.asList(expr);
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitParameterizedType(ParameterizedType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);

        // The type that the parameters are applied to
        ret.addAll(visitType(type.getType()));

        // The actual parameters
        List<Type> arguments = type.typeArguments();
        for(Type argument : arguments)
        {
            ret.addAll(visitType(argument));
        }

        return ret;
    }

    private static List<ASTNode> visitReturnStatement(ReturnStatement stmt)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        return ret;
    }

    private static List<ASTNode> visitStatement(Statement stmt)
    {
        // TODO Finish
        if(stmt instanceof ExpressionStatement)
        {
            return visitExpressionStatement((ExpressionStatement)stmt);
        }
        else if(stmt instanceof ReturnStatement)
        {
            return visitReturnStatement((ReturnStatement)stmt);
        }
        else if(stmt instanceof TryStatement)
        {
            return visitTryStatement((TryStatement)stmt);
        }
        else if(stmt instanceof VariableDeclarationStatement)
        {
            return visitVariableDeclarationStatement((VariableDeclarationStatement)stmt);
        }
        else
        {
            return throwIncompatibleJavaException("visitStatement");
        }
    }

    private static List<ASTNode> visitStringLiteral(StringLiteral expr)
    {
        return Arrays.asList(expr);
    }

    private static List<ASTNode> visitTryResource(ASTNode resource)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(resource);

        if(resource instanceof Name)
        {
            ret.addAll(visitName((Name)resource));
        }
        else if(resource instanceof VariableDeclarationExpression)
        {
            ret.addAll(visitVariableDeclarationExpression((VariableDeclarationExpression)resource));
        }
        else
        {
            return throwIncompatibleJavaException("visitTryResource");
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitTryStatement(TryStatement stmt)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(stmt);

        // Resources for try-with-resources
        List<ASTNode> resources = stmt.resources();
        for(ASTNode resource : resources)
        {
            ret.addAll(visitTryResource(resource));
        }

        ret.addAll(visitBlock(stmt.getBody()));

        List<CatchClause> catches = stmt.catchClauses();
        for(CatchClause c : catches)
        {
            ret.addAll(visitCatchClause(c));
        }

        // The finally block - may be null if it isn't used
        ret.addAll(visitBlock(stmt.getFinally()));

        return ret;
    }

    private static List<ASTNode> visitType(Type type)
    {
        // Ignore. Caused by void methods.
        if(type == null)
        {
            return Collections.emptyList();
        }
    
        // TODO Finish
        if(type instanceof ArrayType)
        {
            return visitArrayType((ArrayType)type);
        }
        else if(type instanceof IntersectionType)
        {
            return visitIntersectionType((IntersectionType)type);
        }
        else if(type instanceof ParameterizedType)
        {
            return visitParameterizedType((ParameterizedType)type);
        }
        else if(type instanceof UnionType)
        {
            return visitUnionType((UnionType)type);
        }
        else
        {
            return throwIncompatibleJavaException("visitType");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitUnionType(UnionType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);
    
        List<Type> types = type.types();
        for(Type t : types)
        {
            ret.addAll(visitType(t));
        }
    
        return ret;
    }

    private static List<ASTNode> visitVariableDeclaration(VariableDeclaration decl)
    {
        // TODO If types matter, differentiate between SingleVariableDeclaration
        // and VariableDeclarationFragment
        List<ASTNode> ret = new ArrayList<>();
        ret.add(decl);
        ret.addAll(visitName(decl.getName()));
        ret.addAll(visitExpression(decl.getInitializer()));
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitVariableDeclarationExpression(VariableDeclarationExpression expr)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(expr);

        List<VariableDeclarationFragment> fragments = expr.fragments();
        for(VariableDeclarationFragment fragment : fragments)
        {
            ret.addAll(visitVariableDeclaration(fragment));
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitVariableDeclarationStatement(VariableDeclarationStatement stmt)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(stmt);

        List<VariableDeclarationFragment> fragments = stmt.fragments();
        for(VariableDeclarationFragment fragment : fragments)
        {
            ret.addAll(visitVariableDeclaration(fragment));
        }

        return ret;
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

    private static List<ASTNode> throwIncompatibleJavaException(String method)
    {
        throw new RuntimeException("SequenceGenerator is not compatible with your Java version: " + method + "() has to be updated.");
    }
}
