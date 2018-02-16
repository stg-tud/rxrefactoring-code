package de.tudarmstadt.rxrefactoring.ext.swingworker.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;

/**
 * Description: This visitor collects different {@link ASTNode} types
 * and add them to lists.
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class DiscoveringVisitor extends UnitASTVisitor
{

	private final String classBinaryName;
	private final List<TypeDeclaration> typeDeclarations;
	private final List<FieldDeclaration> fieldDeclarations;
	private final List<Assignment> assignments;
	private final List<VariableDeclarationStatement> varDeclStatements;
	private final List<SimpleName> simpleNames;
	private final List<ClassInstanceCreation> classInstanceCreations;
	private final List<SingleVariableDeclaration> singleVarDeclarations;
	private final List<MethodDeclaration> methodDeclarations;
	private final List<MethodInvocation> methodInvocations;
	
	public DiscoveringVisitor( String classBinaryName )
	{
		this.classBinaryName = classBinaryName;
		typeDeclarations = new ArrayList<>();
		assignments = new ArrayList<>();
		fieldDeclarations = new ArrayList<>();
		methodInvocations = new ArrayList<>();
		varDeclStatements = new ArrayList<>();
		simpleNames = new ArrayList<>();
		classInstanceCreations = new ArrayList<>();
		singleVarDeclarations = new ArrayList<>();
		methodDeclarations = new ArrayList<>();
	}
	
	@Override
	public boolean visit( FieldDeclaration node )
	{
		if (ASTUtils.isTypeOf(node, classBinaryName))
		{
			fieldDeclarations.add( node );
		}
		return true;
	}

	@Override
	public boolean visit( Assignment node )
	{
		Expression leftHandSide = node.getLeftHandSide();
		ITypeBinding type = leftHandSide.resolveTypeBinding();
		if (ASTUtils.isTypeOf( type, classBinaryName))
		{
			assignments.add( node );
		}
		return true;
	}

	@Override
	public boolean visit( VariableDeclarationStatement node)
	{
		ITypeBinding type = node.getType().resolveBinding();
		if (ASTUtils.isTypeOf( type, classBinaryName))
		{
			varDeclStatements.add( node );
		}
		return true;
	}

	@Override
	public boolean visit( SimpleName simpleName)
	{
		ITypeBinding typeBinding = simpleName.resolveTypeBinding();
		IBinding iBinding = simpleName.resolveBinding();
		if ( iBinding != null )
		{
			int kind = iBinding.getKind();
			if (ASTUtils.isTypeOf(typeBinding, classBinaryName) && kind == IBinding.VARIABLE)
			{
				if (!simpleNames.contains(simpleName))
				{
					simpleNames.add(simpleName);
				}
			}
		}
		return true;
	}

	@Override
	public boolean visit( ClassInstanceCreation node)
	{
		ITypeBinding type = node.getType().resolveBinding();
		if (ASTUtils.isTypeOf( type, classBinaryName))
		{
			classInstanceCreations.add(node);
		}
		return true;
	}

	@Override
	public boolean visit( TypeDeclaration node )
	{
		if (ASTUtils.isTypeOf( node, classBinaryName))
		{
			typeDeclarations.add(node);
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node)
	{
		IMethodBinding binding = node.resolveBinding();
		if (binding != null) 
		{
			ITypeBinding returnType = binding.getReturnType();
	
			if (ASTUtils.isTypeOf(returnType, classBinaryName))
			{
				methodDeclarations.add(node);
			}
		}
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node)
	{
		IMethodBinding binding = node.resolveMethodBinding();
		if (binding != null )
		{
			ITypeBinding type = binding.getDeclaringClass();
			if (ASTUtils.isTypeOf(type, classBinaryName))
			{
				methodInvocations.add(node);
			}
		}

		for (Object arg : node.arguments())
		{
			if (arg instanceof SimpleName)
			{
				SimpleName simpleName = (SimpleName) arg;
				ITypeBinding argType = simpleName.resolveTypeBinding();
				if (ASTUtils.isTypeOf( argType, classBinaryName))
				{
					if (!simpleNames.contains(simpleName))
					{
						simpleNames.add(simpleName);
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node)
	{
		ITypeBinding type = node.getType().resolveBinding();
		if (ASTUtils.isTypeOf(type, classBinaryName))
		{
			singleVarDeclarations.add(node);
		}
		return true;
	}
	
	public List<TypeDeclaration> getTypeDeclarations()
	{
		return typeDeclarations;
	}

	public List<FieldDeclaration> getFieldDeclarations()
	{
		return fieldDeclarations;
	}

	public List<Assignment> getAssignments()
	{
		return assignments;
	}

	public List<VariableDeclarationStatement> getVarDeclStatements()
	{
		return varDeclStatements;
	}

	public List<SimpleName> getSimpleNames()
	{
		return simpleNames;
	}

	public List<ClassInstanceCreation> getClassInstanceCreations()
	{
		return classInstanceCreations;
	}

	public List<SingleVariableDeclaration> getSingleVarDeclarations()
	{
		return singleVarDeclarations;
	}

	public List<MethodInvocation> getMethodInvocations()
	{
		return methodInvocations;
	}

	public List<MethodDeclaration> getMethodDeclarations()
	{
		return methodDeclarations;
	}
	
}
