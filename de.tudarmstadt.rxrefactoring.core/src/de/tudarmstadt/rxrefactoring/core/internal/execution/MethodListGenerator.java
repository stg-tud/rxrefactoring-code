package de.tudarmstadt.rxrefactoring.core.internal.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotatableType;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Initializer;
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
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
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

@SuppressWarnings("unchecked")
public class MethodListGenerator
{
    public static void printMethodLists(ProjectUnits units)
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
                        List<ASTNode> nodes = visitMethodDeclaration(method);
                        for(ASTNode node : nodes)
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
                                System.err.println(className + "." + method.getName().getFullyQualifiedName() + "(" + params + ")");
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private static List<ASTNode> visitAbstractTypeDeclaration(AbstractTypeDeclaration decl)
    {
        if(decl instanceof AnnotationTypeDeclaration)
        {
            return visitAnnotationTypeDeclaration((AnnotationTypeDeclaration)decl);
        }
        else if(decl instanceof EnumDeclaration)
        {
            return visitEnumDeclaration((EnumDeclaration)decl);
        }
        else if(decl instanceof TypeDeclaration)
        {
            return visitTypeDeclaration((TypeDeclaration)decl);
        }
        else
        {
            return throwIncompatibleJavaException("visitAbstractTypeDeclaration");
        }
    }

    private static <T extends ASTNode> List<ASTNode> visitAll(List<T> objects, Function<T, List<ASTNode>> visitor)
    {
        List<ASTNode> ret = new ArrayList<>();
        for(T object : objects)
        {
            ret.addAll(visitor.apply(object));
        }
        return ret;
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

    private static List<ASTNode> visitAnnotationTypeDeclaration(AnnotationTypeDeclaration decl)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), MethodListGenerator::visitExtendedModifier));
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitAll(decl.bodyDeclarations(), MethodListGenerator::visitBodyDeclaration));
        return ret;
    }

    private static List<ASTNode> visitAnnotationTypeMemberDeclaration(AnnotationTypeMemberDeclaration decl)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), MethodListGenerator::visitExtendedModifier));
        ret.addAll(visitType(decl.getType()));
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitExpression(decl.getDefault()));
        return ret;
    }

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
        ret.addAll(visitAll(decl.bodyDeclarations(), MethodListGenerator::visitBodyDeclaration));
        return ret;
    }

    private static List<ASTNode> visitArrayType(ArrayType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);
        ret.addAll(visitType(type.getElementType()));
        ret.addAll(visitAll(type.dimensions(), MethodListGenerator::visitDimension));
        return ret;
    }

    private static List<ASTNode> visitBlock(Block block)
    {
        // Ignore. Caused by missing finally blocks.
        if(block == null)
        {
            return Collections.emptyList();
        }

        List<ASTNode> ret = new ArrayList<>();
        ret.add(block);
        ret.addAll(visitAll(block.statements(), MethodListGenerator::visitStatement));
        return ret;
    }

    private static List<ASTNode> visitBodyDeclaration(BodyDeclaration decl)
    {
        // TODO Finish
        if(decl instanceof AbstractTypeDeclaration)
        {
            return visitAbstractTypeDeclaration((AbstractTypeDeclaration)decl);
        }
        else if(decl instanceof AnnotationTypeMemberDeclaration)
        {
            return visitAnnotationTypeMemberDeclaration((AnnotationTypeMemberDeclaration)decl);
        }
        else if(decl instanceof EnumConstantDeclaration)
        {
            return visitEnumConstantDeclaration((EnumConstantDeclaration)decl);
        }
        else if(decl instanceof FieldDeclaration)
        {
            return visitFieldDeclaration((FieldDeclaration)decl);
        }
        else if(decl instanceof Initializer)
        {
            return visitInitializer((Initializer)decl);
        }
        else if(decl instanceof MethodDeclaration)
        {
            return visitMethodDeclaration((MethodDeclaration)decl);
        }
        else
        {
            return throwIncompatibleJavaException("visitBodyDeclaration");
        }
    }

    private static List<ASTNode> visitCatchClause(CatchClause stmt)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(stmt);
        ret.addAll(visitSingleVariableDeclaration(stmt.getException()));
        ret.addAll(visitBlock(stmt.getBody()));
        return ret;
    }

    private static List<ASTNode> visitClassInstanceCreation(ClassInstanceCreation expr)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getExpression()));
        ret.addAll(visitAll(expr.typeArguments(), MethodListGenerator::visitType));
        ret.addAll(visitType(expr.getType()));
        ret.addAll(visitAll(expr.arguments(), MethodListGenerator::visitExpression));
        ret.addAll(visitAnonymousClassDeclaration(expr.getAnonymousClassDeclaration()));
        return ret;
    }

    private static List<ASTNode> visitDimension(Dimension dim)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(dim);
        ret.addAll(visitAll(dim.annotations(), MethodListGenerator::visitAnnotation));
        return ret;
    }

    private static List<ASTNode> visitEnumConstantDeclaration(EnumConstantDeclaration decl)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), MethodListGenerator::visitExtendedModifier));
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitAll(decl.arguments(), MethodListGenerator::visitExpression));
        ret.addAll(visitAnonymousClassDeclaration(decl.getAnonymousClassDeclaration()));
        return ret;
    }

    private static List<ASTNode> visitEnumDeclaration(EnumDeclaration decl)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), MethodListGenerator::visitExtendedModifier));
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitAll(decl.superInterfaceTypes(), MethodListGenerator::visitType));
        ret.addAll(visitAll(decl.enumConstants(), MethodListGenerator::visitEnumConstantDeclaration));
        ret.addAll(visitAll(decl.bodyDeclarations(), MethodListGenerator::visitBodyDeclaration));
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

    private static List<ASTNode> visitFieldDeclaration(FieldDeclaration decl)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), MethodListGenerator::visitExtendedModifier));
        ret.addAll(visitType(decl.getType()));
        ret.addAll(visitAll(decl.fragments(), MethodListGenerator::visitVariableDeclarationFragment));
        return ret;
    }

    private static List<ASTNode> visitInitializer(Initializer init)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(init);
        ret.addAll(visitBlock(init.getBody()));
        return ret;
    }

    private static List<ASTNode> visitIntersectionType(IntersectionType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);
        ret.addAll(visitAll(type.types(), MethodListGenerator::visitType));
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

    private static List<ASTNode> visitLambdaExpression(LambdaExpression expr)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(expr);
        ret.addAll(visitAll(expr.parameters(), MethodListGenerator::visitVariableDeclaration));
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
        ret.addAll(visitSimpleName(pair.getName()));
        ret.addAll(visitExpression(pair.getValue()));
        return ret;
    }

    private static List<ASTNode> visitMethodDeclaration(MethodDeclaration method)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(method);
        ret.addAll(visitAll(method.modifiers(), MethodListGenerator::visitExtendedModifier));
        ret.addAll(visitAll(method.typeParameters(), MethodListGenerator::visitTypeParameter));
        ret.addAll(visitType(method.getReturnType2()));
        ret.addAll(visitSimpleName(method.getName()));
        ret.addAll(visitType(method.getReceiverType()));
        ret.addAll(visitSimpleName(method.getReceiverQualifier()));
        ret.addAll(visitAll(method.parameters(), MethodListGenerator::visitSingleVariableDeclaration));
        ret.addAll(visitAll(method.extraDimensions(), MethodListGenerator::visitDimension));
        ret.addAll(visitAll(method.thrownExceptionTypes(), MethodListGenerator::visitType));
        ret.addAll(visitBlock(method.getBody()));
        return ret;
    }

    private static List<ASTNode> visitMethodInvocation(MethodInvocation expr)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getExpression()));
        ret.addAll(visitAll(expr.typeArguments(), MethodListGenerator::visitType));
        ret.addAll(visitSimpleName(expr.getName()));
        ret.addAll(visitAll(expr.arguments(), MethodListGenerator::visitExpression));
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

        if(name instanceof QualifiedName)
        {
            return visitQualifiedName((QualifiedName)name);
        }
        else if(name instanceof SimpleName)
        {
            return visitSimpleName((SimpleName)name);
        }
        else
        {
            return throwIncompatibleJavaException("visitName");
        }
    }

    private static List<ASTNode> visitNameQualifiedType(NameQualifiedType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);
        ret.addAll(visitName(type.getQualifier()));
        ret.addAll(visitAll(type.annotations(), MethodListGenerator::visitAnnotation));
        ret.addAll(visitSimpleName(type.getName()));
        return ret;
    }

    private static List<ASTNode> visitNormalAnnotation(NormalAnnotation annotation)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(annotation);
        ret.addAll(visitName(annotation.getTypeName()));
        ret.addAll(visitAll(annotation.values(), MethodListGenerator::visitMemberValuePair));
        return ret;
    }

    private static List<ASTNode> visitNumberLiteral(NumberLiteral expr)
    {
        return Arrays.asList(expr);
    }

    private static List<ASTNode> visitParameterizedType(ParameterizedType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);
        ret.addAll(visitType(type.getType()));
        ret.addAll(visitAll(type.typeArguments(), MethodListGenerator::visitType));
        return ret;
    }

    private static List<ASTNode> visitPrimitiveType(PrimitiveType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);
        ret.addAll(visitAll(type.annotations(), MethodListGenerator::visitAnnotation));
        return ret;
    }

    private static List<ASTNode> visitQualifiedName(QualifiedName name)
    {
        // Ignore. Caused by explicit receivers in methods.
        if(name == null)
        {
            return Collections.emptyList();
        }
    
        List<ASTNode> ret = new ArrayList<>();
        ret.add(name);
        ret.addAll(visitName(name.getQualifier()));
        ret.addAll(visitSimpleName(name.getName()));
        return ret;
    }

    private static List<ASTNode> visitQualifiedType(QualifiedType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);
        ret.addAll(visitType(type.getQualifier()));
        ret.addAll(visitAll(type.annotations(), MethodListGenerator::visitAnnotation));
        ret.addAll(visitSimpleName(type.getName()));
        return ret;
    }

    private static List<ASTNode> visitReturnStatement(ReturnStatement stmt)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        return ret;
    }

    private static List<ASTNode> visitSimpleName(SimpleName name)
    {
        // Ignore. Caused by explicit receivers in methods.
        if(name == null)
        {
            return Collections.emptyList();
        }
    
        return Arrays.asList(name);
    }

    private static List<ASTNode> visitSimpleType(SimpleType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);
        ret.addAll(visitAll(type.annotations(), MethodListGenerator::visitAnnotation));
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

    private static List<ASTNode> visitSingleVariableDeclaration(SingleVariableDeclaration decl)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), MethodListGenerator::visitExtendedModifier));
        ret.addAll(visitType(decl.getType()));
        ret.addAll(visitAll(decl.varargsAnnotations(), MethodListGenerator::visitAnnotation));
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitAll(decl.extraDimensions(), MethodListGenerator::visitDimension));
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
        if(resource instanceof Name)
        {
            return visitName((Name)resource);
        }
        else if(resource instanceof VariableDeclarationExpression)
        {
            return visitVariableDeclarationExpression((VariableDeclarationExpression)resource);
        }
        else
        {
            return throwIncompatibleJavaException("visitTryResource");
        }
    }

    private static List<ASTNode> visitTryStatement(TryStatement stmt)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(stmt);
        ret.addAll(visitAll(stmt.resources(), MethodListGenerator::visitTryResource));
        ret.addAll(visitBlock(stmt.getBody()));
        ret.addAll(visitAll(stmt.catchClauses(), MethodListGenerator::visitCatchClause));
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

    private static List<ASTNode> visitTypeDeclaration(TypeDeclaration decl)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), MethodListGenerator::visitExtendedModifier));
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitAll(decl.typeParameters(), MethodListGenerator::visitTypeParameter));
        ret.addAll(visitType(decl.getSuperclassType()));
        ret.addAll(visitAll(decl.superInterfaceTypes(), MethodListGenerator::visitType));
        ret.addAll(visitAll(decl.bodyDeclarations(), MethodListGenerator::visitBodyDeclaration));
        return null;
    }

    private static List<ASTNode> visitTypeParameter(TypeParameter typeParameter)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(typeParameter);
        ret.addAll(visitAll(typeParameter.modifiers(), MethodListGenerator::visitExtendedModifier));
        ret.addAll(visitName(typeParameter.getName()));
        ret.addAll(visitAll(typeParameter.typeBounds(), MethodListGenerator::visitType));
        return ret;
    }

    private static List<ASTNode> visitUnionType(UnionType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);
        ret.addAll(visitAll(type.types(), MethodListGenerator::visitType));
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
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitAll(decl.extraDimensions(), MethodListGenerator::visitDimension));
        ret.addAll(visitExpression(decl.getInitializer()));
        return ret;
    }

    private static List<ASTNode> visitVariableDeclarationExpression(VariableDeclarationExpression expr)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(expr);
        ret.addAll(visitAll(expr.modifiers(), MethodListGenerator::visitExtendedModifier));
        ret.addAll(visitType(expr.getType()));
        ret.addAll(visitAll(expr.fragments(), MethodListGenerator::visitVariableDeclarationFragment));
        return ret;
    }

    private static List<ASTNode> visitVariableDeclarationStatement(VariableDeclarationStatement stmt)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(stmt);
        ret.addAll(visitAll(stmt.modifiers(), MethodListGenerator::visitExtendedModifier));
        ret.addAll(visitType(stmt.getType()));
        ret.addAll(visitAll(stmt.fragments(), MethodListGenerator::visitVariableDeclarationFragment));
        return ret;
    }

    private static List<ASTNode> visitWildcardType(WildcardType type)
    {
        List<ASTNode> ret = new ArrayList<>();
        ret.add(type);
        ret.addAll(visitAll(type.annotations(), MethodListGenerator::visitAnnotation));
        ret.addAll(visitType(type.getBound()));
        return ret;
    }

    // Basically a fixed version of IRewriteCompilationUnit.getRewrittenNode
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
