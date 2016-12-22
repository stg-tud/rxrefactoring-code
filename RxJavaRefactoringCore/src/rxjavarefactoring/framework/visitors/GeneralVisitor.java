package rxjavarefactoring.framework.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.utils.ASTUtil;

/**
 * Description: This visitor collects different ASTNode types
 * and add them to lists.
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class GeneralVisitor extends ASTVisitor
{
	private final String classBinaryName;
	private final List<TypeDeclaration> subclasses;
	private final List<AnonymousClassDeclaration> anonymousClasses;
	private final List<VariableDeclaration> variableDeclarations;
	private final List<Assignment> assignments;
	private final List<FieldDeclaration> fieldDeclarations;
	private final List<MethodInvocation> methodInvocations;

	public GeneralVisitor( String classBinaryName )
	{
		this.classBinaryName = classBinaryName;
		subclasses = new ArrayList<>();
		anonymousClasses = new ArrayList<>();
		variableDeclarations = new ArrayList<>();
		assignments = new ArrayList<>();
		fieldDeclarations = new ArrayList<>();
		methodInvocations = new ArrayList<>();
	}

	@Override
	public boolean visit( TypeDeclaration node )
	{
		if ( ASTUtil.isTypeOf( node, classBinaryName ) )
		{
			subclasses.add( node );
		}
		return true;
	}

	@Override
	public boolean visit( FieldDeclaration node )
	{
		if ( ASTUtil.isTypeOf( node, classBinaryName ) )
		{
			fieldDeclarations.add( node );
		}
		return true;
	}

	@Override
	public boolean visit( AnonymousClassDeclaration node )
	{
		if ( ASTUtil.isTypeOf( node, classBinaryName ) )
		{
			VariableDeclaration varDeclParent = ASTUtil.findParent( node, VariableDeclaration.class );
			if ( varDeclParent != null )
			{
				variableDeclarations.add( varDeclParent );
				return true;
			}

			Assignment assignmentParent = ASTUtil.findParent( node, Assignment.class );
			if ( assignmentParent != null )
			{
				assignments.add( assignmentParent );
				return true;
			}

			anonymousClasses.add( node );
		}
		return true;
	}

	@Override
	public boolean visit( MethodInvocation node )
	{
		IMethodBinding binding = node.resolveMethodBinding();
		if ( binding == null )
		{
			return true;
		}

		ITypeBinding declaringClass = binding.getDeclaringClass();
		if ( declaringClass == null )
		{
			return true;
		}

		if ( ASTUtil.isTypeOf( declaringClass, classBinaryName ) )
		{
			methodInvocations.add( node );
		}

		return true;
	}

	/**
	 * Example: public class MyClass extends TargetClass { ... }
	 * 
	 * @return List of type declarations of the class extending TargetClass
	 */
	public List<TypeDeclaration> getSubclasses()
	{
		return subclasses;
	}

	/**
	 * Example: new TargetClass(){...}
	 * 
	 * @return List of anonymous class declarations of TargetClass
	 */
	public List<AnonymousClassDeclaration> getAnonymousClasses()
	{
		return anonymousClasses;
	}

	/**
	 * Example: TargetClass target = new TargetClass(){...}
	 * 
	 * @return List of variable declarations of TargetClass
	 */
	public List<VariableDeclaration> getVariableDeclarations()
	{
		return variableDeclarations;
	}

	/**
	 * Example: target = new TargetClass(){...}
	 * 
	 * @return List of assignments of TargetClass
	 */
	public List<Assignment> getAssignments()
	{
		return assignments;
	}

	/**
	 * Example: TargetClass target; do not confuse with variable declarations
	 *
	 * @return List of field declarations of TargetClass
	 */
	public List<FieldDeclaration> getFieldDeclarations()
	{
		return fieldDeclarations;
	}

	/**
	 * Example: someclassInstance.invokeSomeMethod();
	 * 
	 * @return List of methods invocations of TargetClass
	 */
	public List<MethodInvocation> getMethodInvocations()
	{
		return methodInvocations;
	}
}
