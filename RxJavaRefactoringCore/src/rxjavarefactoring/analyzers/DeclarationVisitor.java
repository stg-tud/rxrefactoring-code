package rxjavarefactoring.analyzers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.utils.ASTUtil;

/**
 * Description: This visitor collects all class declarations and groups then
 * into 3 groups:<br>
 * <ul>
 * <li>TypeDeclarations: Classes that extend the target class</li>
 * <li>AnonymousClassDeclarations: Target classes that are instantiated without
 * assigning the object to a variable (fire and forget)</li>
 * <li>VariableDeclarations: Target classes that are assigned to a variable
 * after instantiation</li>
 * </ul>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class DeclarationVisitor extends ASTVisitor
{
	private final String classBinaryName;
	private final List<TypeDeclaration> subclasses;
	private final List<AnonymousClassDeclaration> anonymousClasses;
	private final List<VariableDeclaration> variableDeclarations;
	private final List<Assignment> assignments;
	private final List<FieldDeclaration> fieldDeclarations;

	public DeclarationVisitor( String classBinaryName )
	{
		this.classBinaryName = classBinaryName;
		subclasses = new ArrayList<>();
		anonymousClasses = new ArrayList<>();
		variableDeclarations = new ArrayList<>();
		assignments = new ArrayList<>();
		fieldDeclarations = new ArrayList<>();
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

	/**
	 * Subclasses correspond to Java objects that extend the target class<br>
	 * Example: public class MyClass extends TargetClass { ... }
	 * 
	 * @return A type declaration of the class extending TargetClass
	 */
	public List<TypeDeclaration> getSubclasses()
	{
		return subclasses;
	}

	/**
	 * AnonymousClasses correspond to class instance creations without assigning
	 * the value to a variable.<br>
	 * Example: new TargetClass(){...}
	 * 
	 * @return An anonymous class declaration of TargetClass
	 */
	public List<AnonymousClassDeclaration> getAnonymousClasses()
	{
		return anonymousClasses;
	}

	/**
	 * VariableDeclarations correspond to class instance creations that are
	 * assigned to a variable.<br>
	 * Example: TargetClass target = new TargetClass(){...}
	 * 
	 * @return A variable declaration of TargetClass
	 */
	public List<VariableDeclaration> getVariableDeclarations()
	{
		return variableDeclarations;
	}

	/**
	 * Assigments correspond to class instance creations that are assigned
	 * without specifying the variable type.<br>
	 * Example: target = new TargetClass(){...}
	 * 
	 * @return An assignment of TargetClass
	 */
	public List<Assignment> getAssignments()
	{
		return assignments;
	}

	public boolean isTargetClassFound()
	{
		return !subclasses.isEmpty() ||
				!anonymousClasses.isEmpty() ||
				!variableDeclarations.isEmpty();
	}
}
