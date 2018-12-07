package de.tudarmstadt.rxrefactoring.core.internal.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotatableType;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;
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
                    for(MethodDeclaration method : type.getMethods())
                    {
                        for(ASTNode node : visitMethodDeclaration(method))
                        {
                            if(nodeHasChanges(node, unit))
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
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private static List<ASTNode> visitAnnotatableType(AnnotatableType type)
    {
        if(type instanceof NameQualifiedType)
        {
            return visitNameQualifiedType((NameQualifiedType)type);
        }
        else if(type instanceof PrimitiveType)
        {
            return visitPrimitiveType((PrimitiveType)type);
        }
        else if(type instanceof QualifiedType)
        {
            return visitQualifiedType((QualifiedType)type);
        }
        else if(type instanceof SimpleType)
        {
            return visitSimpleType((SimpleType)type);
        }
        else if(type instanceof WildcardType)
        {
            return visitWildcardType((WildcardType)type);
        }
        else
        {
            return throwIncompatibleJavaException("visitAnnotatableType");
        }
    }

    private static List<ASTNode> visitAnnotation(Annotation annotation)
    {
        if(annotation instanceof MarkerAnnotation)
        {
            return visitMarkerAnnotation((MarkerAnnotation)annotation);
        }
        else if(annotation instanceof NormalAnnotation)
        {
            return visitNormalAnnotation((NormalAnnotation)annotation);
        }
        else if(annotation instanceof SingleMemberAnnotation)
        {
            return visitSingleMemberAnnotation((SingleMemberAnnotation)annotation);
        }
        else
        {
            return throwIncompatibleJavaException("visitAnnotation");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitAnonymousClassDeclaration(AnonymousClassDeclaration decl)
    {
        // Ignore. Caused by class instantiation that does not use an anonymous
        // class.
        if(decl == null)
        {
            return Collections.emptyList();
        }
    
        List<ASTNode> ret = new ArrayList<>();
        ret.add(decl);
    
        List<BodyDeclaration> declarations = decl.bodyDeclarations();
        // TODO Implement visitBodyDeclaration
        return ret;
    }

    private static List<ASTNode> visitArrayType(ArrayType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);
        ret.addAll(visitType(type.getElementType()));
        return ret;
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
        ret.addAll(visitSingleVariableDeclaration(stmt.getException()));
        ret.addAll(visitBlock(stmt.getBody()));
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitClassInstanceCreation(ClassInstanceCreation expr)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(expr);
        ret.addAll(visitType(expr.getType()));

        List<Expression> arguments  = expr.arguments();
        for(Expression argument : arguments)
        {
            ret.addAll(visitExpression(argument));
        }

        List<Type> typeArguments = expr.typeArguments();
        for(Type typeArgument : typeArguments)
        {
            ret.addAll(visitType(typeArgument));
        }

        ret.addAll(visitExpression(expr.getExpression()));
        ret.addAll(visitAnonymousClassDeclaration(expr.getAnonymousClassDeclaration()));
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
        if(expr instanceof ClassInstanceCreation)
        {
            return visitClassInstanceCreation((ClassInstanceCreation)expr);
        }
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

    private static List<ASTNode> visitExtendedModifier(IExtendedModifier modifier)
    {
        if(modifier instanceof Annotation)
        {
            return visitAnnotation((Annotation)modifier);
        }
        else if(modifier instanceof Modifier)
        {
            return visitModifier((Modifier)modifier);
        }
        else
        {
            return throwIncompatibleJavaException("visitExtendedModifier");
        }
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

    private static List<ASTNode> visitMarkerAnnotation(MarkerAnnotation annotation)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(annotation);
        ret.addAll(visitName(annotation.getTypeName()));
        return ret;
    }

    private static List<ASTNode> visitMemberValuePair(MemberValuePair pair)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(pair);
        ret.addAll(visitName(pair.getName()));
        ret.addAll(visitExpression(pair.getValue()));
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitMethodDeclaration(MethodDeclaration method)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(method);

        List<IExtendedModifier> modifiers = method.modifiers();
        for(IExtendedModifier modifier : modifiers)
        {
            ret.addAll(visitExtendedModifier(modifier));
        }

        List<TypeParameter> typeParameters =  method.typeParameters();
        for(TypeParameter typeParameter : typeParameters)
        {
            ret.addAll(visitTypeParameter(typeParameter));
        }

        ret.addAll(visitType(method.getReturnType2()));
        ret.addAll(visitName(method.getName()));
        ret.addAll(visitType(method.getReceiverType()));
        ret.addAll(visitName(method.getReceiverQualifier()));

        List<SingleVariableDeclaration> params = method.parameters();
        for(SingleVariableDeclaration param : params)
        {
            ret.addAll(visitSingleVariableDeclaration(param));
        }

        List<Type> exceptions = method.thrownExceptionTypes();
        for(Type exception : exceptions)
        {
            ret.addAll(visitType(exception));
        }

        ret.addAll(visitBlock(method.getBody()));
        return ret;
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

    private static List<ASTNode> visitModifier(Modifier modifier)
    {
        return Arrays.asList(modifier);
    }

    private static List<ASTNode> visitName(Name name)
    {
        // Ignore. Caused by explicit receivers in methods.
        if(name == null)
        {
            return Collections.emptyList();
        }

        return Arrays.asList(name);
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitNameQualifiedType(NameQualifiedType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);

        List<Annotation> annotations = type.annotations();
        for(Annotation annotation : annotations)
        {
            ret.addAll(visitAnnotation(annotation));
        }

        ret.addAll(visitName(type.getName()));
        ret.addAll(visitName(type.getQualifier()));
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitNormalAnnotation(NormalAnnotation annotation)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(annotation);
        ret.addAll(visitName(annotation.getTypeName()));
    
        List<MemberValuePair> pairs = annotation.values();
        for(MemberValuePair pair : pairs)
        {
            ret.addAll(visitMemberValuePair(pair));
        }
    
        return null;
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

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitPrimitiveType(PrimitiveType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);
    
        List<Annotation> annotations = type.annotations();
        for(Annotation annotation : annotations)
        {
            ret.addAll(visitAnnotation(annotation));
        }
    
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitQualifiedType(QualifiedType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);

        List<Annotation> annotations = type.annotations();
        for(Annotation annotation : annotations)
        {
            ret.addAll(visitAnnotation(annotation));
        }

        ret.addAll(visitType(type.getQualifier()));
        ret.addAll(visitName(type.getName()));
        return ret;
    }

    private static List<ASTNode> visitReturnStatement(ReturnStatement stmt)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitSimpleType(SimpleType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);

        List<Annotation> annotations = type.annotations();
        for(Annotation annotation : annotations)
        {
            ret.addAll(visitAnnotation(annotation));
        }

        ret.addAll(visitName(type.getName()));
        return ret;
    }

    private static List<ASTNode> visitSingleMemberAnnotation(SingleMemberAnnotation annotation)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(annotation);
        ret.addAll(visitName(annotation.getTypeName()));
        ret.addAll(visitExpression(annotation.getValue()));
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitSingleVariableDeclaration(SingleVariableDeclaration decl)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(decl);

        List<IExtendedModifier> modifiers = decl.modifiers();
        for(IExtendedModifier modifier : modifiers)
        {
            ret.addAll(visitExtendedModifier(modifier));
        }

        ret.addAll(visitType(decl.getType()));
        ret.addAll(visitName(decl.getName()));
        ret.addAll(visitExpression(decl.getInitializer()));
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
        else if(stmt instanceof ThrowStatement)
        {
            return visitThrowStatement((ThrowStatement)stmt);
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

    private static List<ASTNode> visitThrowStatement(ThrowStatement stmt)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        return ret;
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

        if(type instanceof AnnotatableType)
        {
            return visitAnnotatableType((AnnotatableType)type);
        }
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
    private static List<ASTNode> visitTypeParameter(TypeParameter typeParameter)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(typeParameter);
        ret.addAll(visitName(typeParameter.getName()));

        List<IExtendedModifier> modifiers = typeParameter.modifiers();
        for(IExtendedModifier modifier : modifiers)
        {
            ret.addAll(visitExtendedModifier(modifier));
        }

        List<Type> bounds = typeParameter.typeBounds();
        for(Type bound : bounds)
        {
            ret.addAll(visitType(bound));
        }

        return ret;
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
        if(decl instanceof SingleVariableDeclaration)
        {
            return visitSingleVariableDeclaration((SingleVariableDeclaration)decl);
        }
        else if(decl instanceof VariableDeclarationFragment)
        {
            return visitVariableDeclarationFragment((VariableDeclarationFragment)decl);
        }
        else
        {
            return throwIncompatibleJavaException("visitVariableDeclaration");
        }
    }

    private static List<ASTNode> visitVariableDeclarationFragment(VariableDeclarationFragment decl)
    {
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

        List<IExtendedModifier> modifiers = expr.modifiers();
        for(IExtendedModifier modifier : modifiers)
        {
            ret.addAll(visitExtendedModifier(modifier));
        }

        ret.addAll(visitType(expr.getType()));

        List<VariableDeclarationFragment> fragments = expr.fragments();
        for(VariableDeclarationFragment fragment : fragments)
        {
            ret.addAll(visitVariableDeclarationFragment(fragment));
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitVariableDeclarationStatement(VariableDeclarationStatement stmt)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(stmt);

        List<IExtendedModifier> modifiers = stmt.modifiers();
        for(IExtendedModifier modifier : modifiers)
        {
            ret.addAll(visitExtendedModifier(modifier));
        }

        ret.addAll(visitType(stmt.getType()));

        List<VariableDeclarationFragment> fragments = stmt.fragments();
        for(VariableDeclarationFragment fragment : fragments)
        {
            ret.addAll(visitVariableDeclarationFragment(fragment));
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    private static List<ASTNode> visitWildcardType(WildcardType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);
    
        List<Annotation> annotations = type.annotations();
        for(Annotation annotation : annotations)
        {
            ret.addAll(visitAnnotation(annotation));
        }
    
        ret.addAll(visitType(type.getBound()));
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
