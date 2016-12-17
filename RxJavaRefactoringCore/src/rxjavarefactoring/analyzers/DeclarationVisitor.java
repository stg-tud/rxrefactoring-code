package rxjavarefactoring.analyzers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
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
	private final List<VariableDeclaration> anonymousCachedClasses;

	public DeclarationVisitor( String classBinaryName )
	{
		this.classBinaryName = classBinaryName;
		subclasses = new ArrayList<>();
		anonymousClasses = new ArrayList<>();
		anonymousCachedClasses = new ArrayList<>();
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
	public boolean visit( AnonymousClassDeclaration node )
	{
		if ( ASTUtil.isTypeOf( node, classBinaryName ) )
		{
			VariableDeclaration parent = ASTUtil.findParent( node, VariableDeclaration.class );
			if ( parent == null )
			{
				anonymousClasses.add( node );
			}
			else
			{
				anonymousCachedClasses.add( parent );
			}
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
	 * AnonymousCachedClasses correspond to class instance creations that are
	 * assigned to a variable.<br>
	 * Example: target = new TargetClass(){...}
	 * 
	 * @return A Variable declaration of TargetClass
	 */
	public List<VariableDeclaration> getAnonymousCachedClasses()
	{
		return anonymousCachedClasses;
	}

	public boolean isTargetClassFound()
	{
		return !subclasses.isEmpty() ||
				!anonymousClasses.isEmpty() ||
				!anonymousCachedClasses.isEmpty();
	}
}
