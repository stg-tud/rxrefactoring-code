package de.tudarmstadt.rxrefactoring.core.internal.execution;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.core.dom.*;

import de.tudarmstadt.rxrefactoring.core.internal.execution.filter.FilteredArrayList;
import de.tudarmstadt.rxrefactoring.core.internal.execution.filter.IFilter;

@SuppressWarnings("unchecked")
public final class JavaVisitor
{
    private final IFilter<ASTNode> filter;

    public JavaVisitor(IFilter<ASTNode> filter)
    {
        this.filter = filter;
    }

    public List<ASTNode> visitAbstractTypeDeclaration(AbstractTypeDeclaration decl)
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
            return throwIncompatibleJavaException("visitAbstractTypeDeclaration", decl.getClass());
        }
    }

    public List<ASTNode> visitAnnotatableType(AnnotatableType type)
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
            return throwIncompatibleJavaException("visitAnnotatableType", type.getClass());
        }
    }

    public List<ASTNode> visitAnnotation(Annotation annotation)
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
            return throwIncompatibleJavaException("visitAnnotation", annotation.getClass());
        }
    }

    public List<ASTNode> visitAnnotationTypeDeclaration(AnnotationTypeDeclaration decl)
    {
        List<ASTNode> ret = newList();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), this::visitExtendedModifier));
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitAll(decl.bodyDeclarations(), this::visitBodyDeclaration));
        return ret;
    }

    public List<ASTNode> visitAnnotationTypeMemberDeclaration(AnnotationTypeMemberDeclaration decl)
    {
        List<ASTNode> ret = newList();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), this::visitExtendedModifier));
        ret.addAll(visitType(decl.getType()));
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitExpression(decl.getDefault()));
        return ret;
    }

    public List<ASTNode> visitAnonymousClassDeclaration(AnonymousClassDeclaration decl)
    {
        // Ignore. Caused by class instantiation that does not use an anonymous
        // class.
        if(decl == null)
        {
            return Collections.emptyList();
        }

        List<ASTNode> ret = newList();
        ret.add(decl);
        ret.addAll(visitAll(decl.bodyDeclarations(), this::visitBodyDeclaration));
        return ret;
    }

    public List<ASTNode> visitArrayAccess(ArrayAccess expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getArray()));
        ret.addAll(visitExpression(expr.getIndex()));
        return ret;
    }

    public List<ASTNode> visitArrayCreation(ArrayCreation expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitAll(expr.dimensions(), this::visitDimension));
        ret.addAll(visitArrayType(expr.getType()));
        ret.addAll(visitArrayInitializer(expr.getInitializer()));
        return ret;
    }

    public List<ASTNode> visitArrayInitializer(ArrayInitializer expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitAll(expr.expressions(), this::visitExpression));
        return ret;
    }

    public List<ASTNode> visitArrayType(ArrayType type)
    {
        List<ASTNode> ret = newList();
        ret.add(type);
        ret.addAll(visitType(type.getElementType()));
        ret.addAll(visitAll(type.dimensions(), this::visitDimension));
        return ret;
    }

    public List<ASTNode> visitAssertStatement(AssertStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        ret.addAll(visitExpression(stmt.getMessage()));
        return ret;
    }

    public List<ASTNode> visitAssignment(Assignment expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getLeftHandSide()));
        ret.addAll(visitExpression(expr.getRightHandSide()));
        return ret;
    }

    public List<ASTNode> visitBlock(Block block)
    {
        // Ignore. Caused by missing finally blocks.
        if(block == null)
        {
            return Collections.emptyList();
        }

        List<ASTNode> ret = newList();
        ret.add(block);
        ret.addAll(visitAll(block.statements(), this::visitStatement));
        return ret;
    }

    public List<ASTNode> visitBodyDeclaration(BodyDeclaration decl)
    {
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
            return throwIncompatibleJavaException("visitBodyDeclaration", decl.getClass());
        }
    }

    public List<ASTNode> visitBooleanLiteral(BooleanLiteral literal)
    {
        return Arrays.asList(literal);
    }

    public List<ASTNode> visitBreakStatement(BreakStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.addAll(visitSimpleName(stmt.getLabel()));
        return ret;
    }

    public List<ASTNode> visitCastExpression(CastExpression expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitType(expr.getType()));
        ret.addAll(visitExpression(expr.getExpression()));
        return ret;
    }

    public List<ASTNode> visitCatchClause(CatchClause stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitSingleVariableDeclaration(stmt.getException()));
        ret.addAll(visitBlock(stmt.getBody()));
        return ret;
    }

    public List<ASTNode> visitCharacterLiteral(CharacterLiteral literal)
    {
        return Arrays.asList(literal);
    }

    public List<ASTNode> visitClassInstanceCreation(ClassInstanceCreation expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getExpression()));
        ret.addAll(visitAll(expr.typeArguments(), this::visitType));
        ret.addAll(visitType(expr.getType()));
        ret.addAll(visitAll(expr.arguments(), this::visitExpression));
        ret.addAll(visitAnonymousClassDeclaration(expr.getAnonymousClassDeclaration()));
        return ret;
    }

    public List<ASTNode> visitCompilationUnit(CompilationUnit unit)
    {
        List<ASTNode> ret = newList();
        ret.add(unit);
        ret.addAll(visitModuleDeclaration(unit.getModule()));
        ret.addAll(visitPackageDeclaration(unit.getPackage()));
        ret.addAll(visitAll(unit.imports(), this::visitImportDeclaration));
        ret.addAll(visitAll(unit.types(), this::visitAbstractTypeDeclaration));
        return ret;
    }

    public List<ASTNode> visitConditionalExpression(ConditionalExpression expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getExpression()));
        ret.addAll(visitExpression(expr.getThenExpression()));
        ret.addAll(visitExpression(expr.getElseExpression()));
        return ret;
    }

    public List<ASTNode> visitConstructorInvocation(ConstructorInvocation stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitAll(stmt.typeArguments(), this::visitType));
        ret.addAll(visitAll(stmt.arguments(), this::visitExpression));
        return ret;
    }

    public List<ASTNode> visitContinueStatement(ContinueStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitSimpleName(stmt.getLabel()));
        return ret;
    }

    public List<ASTNode> visitCreationReference(CreationReference expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitType(expr.getType()));
        ret.addAll(visitAll(expr.typeArguments(), this::visitType));
        return ret;
    }

    public List<ASTNode> visitDimension(Dimension dim)
    {
        List<ASTNode> ret = newList();
        ret.add(dim);
        ret.addAll(visitAll(dim.annotations(), this::visitAnnotation));
        return ret;
    }

    public List<ASTNode> visitDoStatement(DoStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        ret.addAll(visitStatement(stmt.getBody()));
        return ret;
    }

    public List<ASTNode> visitEmptyStatement(EmptyStatement stmt)
    {
        return Arrays.asList(stmt);
    }

    public List<ASTNode> visitEnhancedForStatement(EnhancedForStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitSingleVariableDeclaration(stmt.getParameter()));
        ret.addAll(visitExpression(stmt.getExpression()));
        ret.addAll(visitStatement(stmt.getBody()));
        return ret;
    }

    public List<ASTNode> visitEnumConstantDeclaration(EnumConstantDeclaration decl)
    {
        List<ASTNode> ret = newList();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), this::visitExtendedModifier));
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitAll(decl.arguments(), this::visitExpression));
        ret.addAll(visitAnonymousClassDeclaration(decl.getAnonymousClassDeclaration()));
        return ret;
    }

    public List<ASTNode> visitEnumDeclaration(EnumDeclaration decl)
    {
        List<ASTNode> ret = newList();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), this::visitExtendedModifier));
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitAll(decl.superInterfaceTypes(), this::visitType));
        ret.addAll(visitAll(decl.enumConstants(), this::visitEnumConstantDeclaration));
        ret.addAll(visitAll(decl.bodyDeclarations(), this::visitBodyDeclaration));
        return ret;
    }

    public List<ASTNode> visitExportsDirective(ExportsDirective dir)
    {
        List<ASTNode> ret = newList();
        ret.add(dir);
        ret.addAll(visitName(dir.getName()));
        ret.addAll(visitAll(dir.modules(), this::visitName));
        return ret;
    }

    public List<ASTNode> visitExpression(Expression expr)
    {
        // Ignore. Caused by statements like 'return;'
        if(expr == null)
        {
            return Collections.emptyList();
        }

        if(expr instanceof Annotation)
        {
            return visitAnnotation((Annotation)expr);
        }
        else if(expr instanceof ArrayAccess)
        {
            return visitArrayAccess((ArrayAccess)expr);
        }
        else if(expr instanceof ArrayCreation)
        {
            return visitArrayCreation((ArrayCreation)expr);
        }
        else if(expr instanceof ArrayInitializer)
        {
            return visitArrayInitializer((ArrayInitializer)expr);
        }
        else if(expr instanceof Assignment)
        {
            return visitAssignment((Assignment)expr);
        }
        else if(expr instanceof BooleanLiteral)
        {
            return visitBooleanLiteral((BooleanLiteral)expr);
        }
        else if(expr instanceof CastExpression)
        {
            return visitCastExpression((CastExpression)expr);
        }
        else if(expr instanceof CharacterLiteral)
        {
            return visitCharacterLiteral((CharacterLiteral)expr);
        }
        else if(expr instanceof ClassInstanceCreation)
        {
            return visitClassInstanceCreation((ClassInstanceCreation)expr);
        }
        else if(expr instanceof ConditionalExpression)
        {
            return visitConditionalExpression((ConditionalExpression)expr);
        }
        else if(expr instanceof FieldAccess)
        {
            return visitFieldAccess((FieldAccess)expr);
        }
        else if(expr instanceof InfixExpression)
        {
            return visitInfixExpression((InfixExpression)expr);
        }
        else if(expr instanceof InstanceofExpression)
        {
            return visitInstanceOfExpression((InstanceofExpression)expr);
        }
        else if(expr instanceof LambdaExpression)
        {
            return visitLambdaExpression((LambdaExpression)expr);
        }
        else if(expr instanceof MethodInvocation)
        {
            return visitMethodInvocation((MethodInvocation)expr);
        }
        else if(expr instanceof MethodReference)
        {
            return visitMethodReference((MethodReference)expr);
        }
        else if(expr instanceof Name)
        {
            return visitName((Name)expr);
        }
        else if(expr instanceof NullLiteral)
        {
            return visitNullLiteral((NullLiteral)expr);
        }
        else if(expr instanceof NumberLiteral)
        {
            return visitNumberLiteral((NumberLiteral)expr);
        }
        else if(expr instanceof ParenthesizedExpression)
        {
            return visitParenthesizedExpression((ParenthesizedExpression)expr);
        }
        else if(expr instanceof PostfixExpression)
        {
            return visitPostfixExpression((PostfixExpression)expr);
        }
        else if(expr instanceof PrefixExpression)
        {
            return visitPrefixExpression((PrefixExpression)expr);
        }
        else if(expr instanceof StringLiteral)
        {
            return visitStringLiteral((StringLiteral)expr);
        }
        else if(expr instanceof SuperFieldAccess)
        {
            return visitSuperFieldAccess((SuperFieldAccess)expr);
        }
        else if(expr instanceof SuperMethodInvocation)
        {
            return visitSuperMethodInvocation((SuperMethodInvocation)expr);
        }
        else if(expr instanceof ThisExpression)
        {
            return visitThisExpression((ThisExpression)expr);
        }
        else if(expr instanceof TypeLiteral)
        {
            return visitTypeLiteral((TypeLiteral)expr);
        }
        else if(expr instanceof VariableDeclarationExpression)
        {
            return visitVariableDeclarationExpression((VariableDeclarationExpression)expr);
        }
        else
        {
            return throwIncompatibleJavaException("visitExpression", expr.getClass());
        }
    }

    public List<ASTNode> visitExpressionMethodReference(ExpressionMethodReference expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getExpression()));
        ret.addAll(visitAll(expr.typeArguments(), this::visitType));
        ret.addAll(visitSimpleName(expr.getName()));
        return ret;
    }

    public List<ASTNode> visitExpressionStatement(ExpressionStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        return ret;
    }

    public List<ASTNode> visitExtendedModifier(IExtendedModifier modifier)
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
            return throwIncompatibleJavaException("visitExtendedModifier", (Class<? extends ASTNode>)modifier.getClass());
        }
    }

    public List<ASTNode> visitFieldAccess(FieldAccess expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getExpression()));
        ret.addAll(visitSimpleName(expr.getName()));
        return ret;
    }

    public List<ASTNode> visitFieldDeclaration(FieldDeclaration decl)
    {
        List<ASTNode> ret = newList();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), this::visitExtendedModifier));
        ret.addAll(visitType(decl.getType()));
        ret.addAll(visitAll(decl.fragments(), this::visitVariableDeclarationFragment));
        return ret;
    }

    public List<ASTNode> visitForStatement(ForStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitAll(stmt.initializers(), this::visitExpression));
        ret.addAll(visitExpression(stmt.getExpression()));
        ret.addAll(visitAll(stmt.updaters(), this::visitExpression));
        ret.addAll(visitStatement(stmt.getBody()));
        return ret;
    }

    public List<ASTNode> visitIfStatement(IfStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        ret.addAll(visitStatement(stmt.getThenStatement()));
        ret.addAll(visitStatement(stmt.getElseStatement()));
        return ret;
    }

    public List<ASTNode> visitImportDeclaration(ImportDeclaration decl)
    {
        List<ASTNode> ret = newList();
        ret.add(decl);
        ret.addAll(visitName(decl.getName()));
        return ret;
    }

    public List<ASTNode> visitInfixExpression(InfixExpression expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getLeftOperand()));
        ret.addAll(visitExpression(expr.getRightOperand()));
        ret.addAll(visitAll(expr.extendedOperands(), this::visitExpression));
        return ret;
    }

    public List<ASTNode> visitInstanceOfExpression(InstanceofExpression expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getLeftOperand()));
        ret.addAll(visitType(expr.getRightOperand()));
        return ret;
    }

    public List<ASTNode> visitInitializer(Initializer init)
    {
        List<ASTNode> ret = newList();
        ret.add(init);
        ret.addAll(visitBlock(init.getBody()));
        return ret;
    }

    public List<ASTNode> visitIntersectionType(IntersectionType type)
    {
        List<ASTNode> ret = newList();
        ret.add(type);
        ret.addAll(visitAll(type.types(), this::visitType));
        return ret;
    }

    public List<ASTNode> visitLabeledStatement(LabeledStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitSimpleName(stmt.getLabel()));
        ret.addAll(visitStatement(stmt.getBody()));
        return ret;
    }

    public List<ASTNode> visitLambdaBody(ASTNode body)
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
            return throwIncompatibleJavaException("visitLambdaBody", body.getClass());
        }
    }

    public List<ASTNode> visitLambdaExpression(LambdaExpression expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitAll(expr.parameters(), this::visitVariableDeclaration));
        ret.addAll(visitLambdaBody(expr.getBody()));
        return ret;
    }

    public List<ASTNode> visitMarkerAnnotation(MarkerAnnotation annotation)
    {
        List<ASTNode> ret = newList();
        ret.add(annotation);
        ret.addAll(visitName(annotation.getTypeName()));
        return ret;
    }

    public List<ASTNode> visitMemberValuePair(MemberValuePair pair)
    {
        List<ASTNode> ret = newList();
        ret.add(pair);
        ret.addAll(visitSimpleName(pair.getName()));
        ret.addAll(visitExpression(pair.getValue()));
        return ret;
    }

    public List<ASTNode> visitMethodDeclaration(MethodDeclaration method)
    {
        List<ASTNode> ret = newList();
        ret.add(method);
        ret.addAll(visitAll(method.modifiers(), this::visitExtendedModifier));
        ret.addAll(visitAll(method.typeParameters(), this::visitTypeParameter));
        ret.addAll(visitType(method.getReturnType2()));
        ret.addAll(visitSimpleName(method.getName()));
        ret.addAll(visitType(method.getReceiverType()));
        ret.addAll(visitSimpleName(method.getReceiverQualifier()));
        ret.addAll(visitAll(method.parameters(), this::visitSingleVariableDeclaration));
        ret.addAll(visitAll(method.extraDimensions(), this::visitDimension));
        ret.addAll(visitAll(method.thrownExceptionTypes(), this::visitType));
        ret.addAll(visitBlock(method.getBody()));
        return ret;
    }

    public List<ASTNode> visitMethodInvocation(MethodInvocation expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getExpression()));
        ret.addAll(visitAll(expr.typeArguments(), this::visitType));
        ret.addAll(visitSimpleName(expr.getName()));
        ret.addAll(visitAll(expr.arguments(), this::visitExpression));
        return ret;
    }

    public List<ASTNode> visitMethodReference(MethodReference expr)
    {
        if(expr instanceof CreationReference)
        {
            return visitCreationReference((CreationReference)expr);
        }
        else if(expr instanceof ExpressionMethodReference)
        {
            return visitExpressionMethodReference((ExpressionMethodReference)expr);
        }
        else if(expr instanceof SuperMethodReference)
        {
            return visitSuperMethodReference((SuperMethodReference)expr);
        }
        else if(expr instanceof TypeMethodReference)
        {
            return visitTypeMethodReference((TypeMethodReference)expr);
        }
        else
        {
            return throwIncompatibleJavaException("visitMethodReference", expr.getClass());
        }
    }

    public List<ASTNode> visitModifier(Modifier modifier)
    {
        return Arrays.asList(modifier);
    }

    public List<ASTNode> visitModuleDeclaration(ModuleDeclaration decl)
    {
        // Ignore. Caused by classes that don't use modules.
        if(decl == null)
        {
            return Collections.emptyList();
        }

        List<ASTNode> ret = newList();
        ret.add(decl);
        ret.addAll(visitAll(decl.annotations(), this::visitAnnotation));
        ret.addAll(visitName(decl.getName()));
        ret.addAll(visitAll(decl.moduleStatements(), this::visitModuleDirective));
        return ret;
    }

    public List<ASTNode> visitModuleDirective(ModuleDirective dir)
    {
        if(dir instanceof ModulePackageAccess)
        {
            return visitModulePackageAccess((ModulePackageAccess)dir);
        }
        else if(dir instanceof ProvidesDirective)
        {
            return visitProvidesDirective((ProvidesDirective)dir);
        }
        else if(dir instanceof RequiresDirective)
        {
            return visitRequiresDirective((RequiresDirective)dir);
        }
        else if(dir instanceof UsesDirective)
        {
            return visitUsesDirective((UsesDirective)dir);
        }
        else
        {
            return throwIncompatibleJavaException("visitModuleDirective", dir.getClass());
        }
    }

    public List<ASTNode> visitModuleModifier(ModuleModifier modifier)
    {
        return Arrays.asList(modifier);
    }

    public List<ASTNode> visitModulePackageAccess(ModulePackageAccess dir)
    {
        if(dir instanceof ExportsDirective)
        {
            return visitExportsDirective((ExportsDirective)dir);
        }
        else if(dir instanceof OpensDirective)
        {
            return visitOpensDirective((OpensDirective)dir);
        }
        else
        {
            return throwIncompatibleJavaException("visitModulePackageAccess", dir.getClass());
        }
    }

    public List<ASTNode> visitName(Name name)
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
            return throwIncompatibleJavaException("visitName", name.getClass());
        }
    }

    public List<ASTNode> visitNameQualifiedType(NameQualifiedType type)
    {
        List<ASTNode> ret = newList();
        ret.add(type);
        ret.addAll(visitName(type.getQualifier()));
        ret.addAll(visitAll(type.annotations(), this::visitAnnotation));
        ret.addAll(visitSimpleName(type.getName()));
        return ret;
    }

    public List<ASTNode> visitNormalAnnotation(NormalAnnotation annotation)
    {
        List<ASTNode> ret = newList();
        ret.add(annotation);
        ret.addAll(visitName(annotation.getTypeName()));
        ret.addAll(visitAll(annotation.values(), this::visitMemberValuePair));
        return ret;
    }

    public List<ASTNode> visitNullLiteral(NullLiteral literal)
    {
        return Arrays.asList(literal);
    }

    public List<ASTNode> visitNumberLiteral(NumberLiteral literal)
    {
        return Arrays.asList(literal);
    }

    public List<ASTNode> visitOpensDirective(OpensDirective dir)
    {
        List<ASTNode> ret = newList();
        ret.add(dir);
        ret.addAll(visitName(dir.getName()));
        ret.addAll(visitAll(dir.modules(), this::visitName));
        return ret;
    }

    public List<ASTNode> visitPackageDeclaration(PackageDeclaration decl)
    {
        // Ignore. Caused by classes with the 'default' package.
        if(decl == null)
        {
            return Collections.emptyList();
        }

        List<ASTNode> ret = newList();
        ret.add(decl);
        ret.addAll(visitAll(decl.annotations(), this::visitAnnotation));
        ret.addAll(visitName(decl.getName()));
        return ret;
    }

    public List<ASTNode> visitParameterizedType(ParameterizedType type)
    {
        List<ASTNode> ret = newList();
        ret.add(type);
        ret.addAll(visitType(type.getType()));
        ret.addAll(visitAll(type.typeArguments(), this::visitType));
        return ret;
    }

    public List<ASTNode> visitParenthesizedExpression(ParenthesizedExpression expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getExpression()));
        return ret;
    }

    public List<ASTNode> visitPostfixExpression(PostfixExpression expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getOperand()));
        return ret;
    }

    public List<ASTNode> visitPrefixExpression(PrefixExpression expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitExpression(expr.getOperand()));
        return ret;
    }

    public List<ASTNode> visitPrimitiveType(PrimitiveType type)
    {
        List<ASTNode> ret = newList();
        ret.add(type);
        ret.addAll(visitAll(type.annotations(), this::visitAnnotation));
        return ret;
    }

    public List<ASTNode> visitProvidesDirective(ProvidesDirective dir)
    {
        List<ASTNode> ret = newList();
        ret.add(dir);
        ret.addAll(visitName(dir.getName()));
        ret.addAll(visitAll(dir.implementations(), this::visitName));
        return ret;
    }

    public List<ASTNode> visitQualifiedName(QualifiedName name)
    {
        // Ignore. Caused by explicit receivers in methods.
        if(name == null)
        {
            return Collections.emptyList();
        }

        List<ASTNode> ret = newList();
        ret.add(name);
        ret.addAll(visitName(name.getQualifier()));
        ret.addAll(visitSimpleName(name.getName()));
        return ret;
    }

    public List<ASTNode> visitQualifiedType(QualifiedType type)
    {
        List<ASTNode> ret = newList();
        ret.add(type);
        ret.addAll(visitType(type.getQualifier()));
        ret.addAll(visitAll(type.annotations(), this::visitAnnotation));
        ret.addAll(visitSimpleName(type.getName()));
        return ret;
    }

    public List<ASTNode> visitRequiresDirective(RequiresDirective dir)
    {
        List<ASTNode> ret = newList();
        ret.add(dir);
        ret.addAll(visitAll(dir.modifiers(), this::visitModuleModifier));
        ret.addAll(visitName(dir.getName()));
        return ret;
    }

    public List<ASTNode> visitReturnStatement(ReturnStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        return ret;
    }

    public List<ASTNode> visitSimpleName(SimpleName name)
    {
        // Ignore. Caused by explicit receivers in methods.
        if(name == null)
        {
            return Collections.emptyList();
        }

        return Arrays.asList(name);
    }

    public List<ASTNode> visitSimpleType(SimpleType type)
    {
        List<ASTNode> ret = newList();
        ret.add(type);
        ret.addAll(visitAll(type.annotations(), this::visitAnnotation));
        ret.addAll(visitName(type.getName()));
        return ret;
    }

    public List<ASTNode> visitSingleMemberAnnotation(SingleMemberAnnotation annotation)
    {
        List<ASTNode> ret = newList();
        ret.add(annotation);
        ret.addAll(visitName(annotation.getTypeName()));
        ret.addAll(visitExpression(annotation.getValue()));
        return ret;
    }

    public List<ASTNode> visitSingleVariableDeclaration(SingleVariableDeclaration decl)
    {
        List<ASTNode> ret = newList();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), this::visitExtendedModifier));
        ret.addAll(visitType(decl.getType()));
        ret.addAll(visitAll(decl.varargsAnnotations(), this::visitAnnotation));
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitAll(decl.extraDimensions(), this::visitDimension));
        ret.addAll(visitExpression(decl.getInitializer()));
        return ret;
    }

    public List<ASTNode> visitStatement(Statement stmt)
    {
        // Ignore. Caused by if statements without an else.
        if(stmt == null)
        {
            return Collections.emptyList();
        }

        if(stmt instanceof AssertStatement)
        {
            return visitAssertStatement((AssertStatement)stmt);
        }
        else if(stmt instanceof Block)
        {
            return visitBlock((Block)stmt);
        }
        else if(stmt instanceof BreakStatement)
        {
            return visitBreakStatement((BreakStatement)stmt);
        }
        else if(stmt instanceof ConstructorInvocation)
        {
            return visitConstructorInvocation((ConstructorInvocation)stmt);
        }
        else if(stmt instanceof ContinueStatement)
        {
            return visitContinueStatement((ContinueStatement)stmt);
        }
        else if(stmt instanceof DoStatement)
        {
            return visitDoStatement((DoStatement)stmt);
        }
        else if(stmt instanceof EmptyStatement)
        {
            return visitEmptyStatement((EmptyStatement)stmt);
        }
        else if(stmt instanceof EnhancedForStatement)
        {
            return visitEnhancedForStatement((EnhancedForStatement)stmt);
        }
        else if(stmt instanceof ExpressionStatement)
        {
            return visitExpressionStatement((ExpressionStatement)stmt);
        }
        else if(stmt instanceof ForStatement)
        {
            return visitForStatement((ForStatement)stmt);
        }
        else if(stmt instanceof IfStatement)
        {
            return visitIfStatement((IfStatement)stmt);
        }
        else if(stmt instanceof LabeledStatement)
        {
            return visitLabeledStatement((LabeledStatement)stmt);
        }
        else if(stmt instanceof ReturnStatement)
        {
            return visitReturnStatement((ReturnStatement)stmt);
        }
        else if(stmt instanceof SuperConstructorInvocation)
        {
            return visitSuperConstructorInvocation((SuperConstructorInvocation)stmt);
        }
        else if(stmt instanceof SwitchCase)
        {
            return visitSwitchCase((SwitchCase)stmt);
        }
        else if(stmt instanceof SwitchStatement)
        {
            return visitSwitchStatement((SwitchStatement)stmt);
        }
        else if(stmt instanceof SynchronizedStatement)
        {
            return visitSynchronizedStatement((SynchronizedStatement)stmt);
        }
        else if(stmt instanceof ThrowStatement)
        {
            return visitThrowStatement((ThrowStatement)stmt);
        }
        else if(stmt instanceof TryStatement)
        {
            return visitTryStatement((TryStatement)stmt);
        }
        else if(stmt instanceof TypeDeclarationStatement)
        {
            return visitTypeDeclarationStatement((TypeDeclarationStatement)stmt);
        }
        else if(stmt instanceof VariableDeclarationStatement)
        {
            return visitVariableDeclarationStatement((VariableDeclarationStatement)stmt);
        }
        else if(stmt instanceof WhileStatement)
        {
            return visitWhileStatement((WhileStatement)stmt);
        }
        else
        {
            return throwIncompatibleJavaException("visitStatement", stmt.getClass());
        }
    }

    public List<ASTNode> visitStringLiteral(StringLiteral literal)
    {
        return Arrays.asList(literal);
    }

    public List<ASTNode> visitSuperConstructorInvocation(SuperConstructorInvocation stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        ret.addAll(visitAll(stmt.typeArguments(), this::visitType));
        ret.addAll(visitAll(stmt.arguments(), this::visitExpression));
        return ret;
    }

    public List<ASTNode> visitSuperFieldAccess(SuperFieldAccess expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitName(expr.getQualifier()));
        ret.addAll(visitSimpleName(expr.getName()));
        return ret;
    }

    public List<ASTNode> visitSuperMethodInvocation(SuperMethodInvocation expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitName(expr.getQualifier()));
        ret.addAll(visitAll(expr.typeArguments(), this::visitType));
        ret.addAll(visitSimpleName(expr.getName()));
        ret.addAll(visitAll(expr.arguments(), this::visitExpression));
        return ret;
    }

    public List<ASTNode> visitSuperMethodReference(SuperMethodReference expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitName(expr.getQualifier()));
        ret.addAll(visitAll(expr.typeArguments(), this::visitType));
        ret.addAll(visitSimpleName(expr.getName()));
        return ret;
    }

    public List<ASTNode> visitSwitchCase(SwitchCase stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        return ret;
    }

    public List<ASTNode> visitSwitchStatement(SwitchStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        ret.addAll(visitAll(stmt.statements(), this::visitStatement));
        return ret;
    }

    public List<ASTNode> visitSynchronizedStatement(SynchronizedStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        ret.addAll(visitBlock(stmt.getBody()));
        return ret;
    }

    public List<ASTNode> visitThisExpression(ThisExpression expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitName(expr.getQualifier()));
        return ret;
    }

    public List<ASTNode> visitThrowStatement(ThrowStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        return ret;
    }

    public List<ASTNode> visitTryResource(ASTNode resource)
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
            return throwIncompatibleJavaException("visitTryResource", resource.getClass());
        }
    }

    public List<ASTNode> visitTryStatement(TryStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitAll(stmt.resources(), this::visitTryResource));
        ret.addAll(visitBlock(stmt.getBody()));
        ret.addAll(visitAll(stmt.catchClauses(), this::visitCatchClause));
        ret.addAll(visitBlock(stmt.getFinally()));
        return ret;
    }

    public List<ASTNode> visitType(Type type)
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
            return throwIncompatibleJavaException("visitType", type.getClass());
        }
    }

    public List<ASTNode> visitTypeDeclaration(TypeDeclaration decl)
    {
        List<ASTNode> ret = newList();
        ret.add(decl);
        ret.addAll(visitAll(decl.modifiers(), this::visitExtendedModifier));
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitAll(decl.typeParameters(), this::visitTypeParameter));
        ret.addAll(visitType(decl.getSuperclassType()));
        ret.addAll(visitAll(decl.superInterfaceTypes(), this::visitType));
        ret.addAll(visitAll(decl.bodyDeclarations(), this::visitBodyDeclaration));
        return ret;
    }

    public List<ASTNode> visitTypeDeclarationStatement(TypeDeclarationStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitAbstractTypeDeclaration(stmt.getDeclaration()));
        return ret;
    }

    public List<ASTNode> visitTypeLiteral(TypeLiteral expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitType(expr.getType()));
        return ret;
    }

    public List<ASTNode> visitTypeMethodReference(TypeMethodReference expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitType(expr.getType()));
        ret.addAll(visitAll(expr.typeArguments(), this::visitType));
        ret.addAll(visitSimpleName(expr.getName()));
        return ret;
    }

    public List<ASTNode> visitTypeParameter(TypeParameter typeParameter)
    {
        List<ASTNode> ret = newList();
        ret.add(typeParameter);
        ret.addAll(visitAll(typeParameter.modifiers(), this::visitExtendedModifier));
        ret.addAll(visitName(typeParameter.getName()));
        ret.addAll(visitAll(typeParameter.typeBounds(), this::visitType));
        return ret;
    }

    public List<ASTNode> visitUnionType(UnionType type)
    {
        List<ASTNode> ret = newList();
        ret.add(type);
        ret.addAll(visitAll(type.types(), this::visitType));
        return ret;
    }

    public List<ASTNode> visitUsesDirective(UsesDirective dir)
    {
        List<ASTNode> ret = newList();
        ret.add(dir);
        ret.addAll(visitName(dir.getName()));
        return ret;
    }

    public List<ASTNode> visitVariableDeclaration(VariableDeclaration decl)
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
            return throwIncompatibleJavaException("visitVariableDeclaration", decl.getClass());
        }
    }

    public List<ASTNode> visitVariableDeclarationFragment(VariableDeclarationFragment decl)
    {
        List<ASTNode> ret = newList();
        ret.add(decl);
        ret.addAll(visitSimpleName(decl.getName()));
        ret.addAll(visitAll(decl.extraDimensions(), this::visitDimension));
        ret.addAll(visitExpression(decl.getInitializer()));
        return ret;
    }

    public List<ASTNode> visitVariableDeclarationExpression(VariableDeclarationExpression expr)
    {
        List<ASTNode> ret = newList();
        ret.add(expr);
        ret.addAll(visitAll(expr.modifiers(), this::visitExtendedModifier));
        ret.addAll(visitType(expr.getType()));
        ret.addAll(visitAll(expr.fragments(), this::visitVariableDeclarationFragment));
        return ret;
    }

    public List<ASTNode> visitVariableDeclarationStatement(VariableDeclarationStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitAll(stmt.modifiers(), this::visitExtendedModifier));
        ret.addAll(visitType(stmt.getType()));
        ret.addAll(visitAll(stmt.fragments(), this::visitVariableDeclarationFragment));
        return ret;
    }

    public List<ASTNode> visitWhileStatement(WhileStatement stmt)
    {
        List<ASTNode> ret = newList();
        ret.add(stmt);
        ret.addAll(visitExpression(stmt.getExpression()));
        ret.addAll(visitStatement(stmt.getBody()));
        return ret;
    }

    public List<ASTNode> visitWildcardType(WildcardType type)
    {
        List<ASTNode> ret = newList();
        ret.add(type);
        ret.addAll(visitAll(type.annotations(), this::visitAnnotation));
        ret.addAll(visitType(type.getBound()));
        return ret;
    }

    private <T extends ASTNode> List<ASTNode> visitAll(List<T> objects, Function<T, List<ASTNode>> visitor)
    {
        // Shouldn't happen, but just in case...
        if(objects == null)
        {
            return Collections.emptyList();
        }

        List<ASTNode> ret = newList();
        for(T object : objects)
        {
            ret.addAll(visitor.apply(object));
        }
        return ret;
    }

    private List<ASTNode> newList()
    {
        return new FilteredArrayList<>(this.filter);
    }

    private List<ASTNode> throwIncompatibleJavaException(String method, Class<? extends ASTNode> clazz)
    {
        throw new RuntimeException(JavaVisitor.class.getSimpleName() + " is not compatible with your Java version: " + method + "() has to be updated, " + clazz.getName() + " could not be handled.");
    }
}
