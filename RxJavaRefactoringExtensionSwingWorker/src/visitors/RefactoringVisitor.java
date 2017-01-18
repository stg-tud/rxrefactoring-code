package visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

import domain.SwingWorkerInfo;
import rxjavarefactoring.framework.utils.ASTUtil;

/**
 * Description: Visitor to extract relevant information about SwingWorkers<br>
 * <ol>
 * <li>doInBackground Block</li>
 * <li>done Block</li>
 * <li>process Block</li>
 * <li>returned type</li>
 * <li>presence of get method and its arguments</li>
 * <li>progress update type</li>
 * </ol>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class RefactoringVisitor extends ASTVisitor
{
	private static final String DO_IN_BACKGROUND = "doInBackground";
	private static final String DONE = "done";
	private static final String PROCESS = "process";

	private Block doInBackgroundBlock;
	private Block doneBlock;
	private Block processBlock;
	private Type resultType;
	private Type processType;
	private String processVariableName;
	private List<SuperMethodInvocation> superMethodInvocationsToRemove;
	private MethodDeclaration constructor;

	// for "stateful" classes
	private List<FieldDeclaration> fieldDeclarations;
	private List<MethodDeclaration> additionalMethodDeclarations;
	private List<MethodDeclaration> allMethodDeclarations;
	private List<TypeDeclaration> typeDeclarations;

	public RefactoringVisitor()
	{
		fieldDeclarations = new ArrayList<>();
		additionalMethodDeclarations = new ArrayList<>();
		allMethodDeclarations = new ArrayList<>();
		superMethodInvocationsToRemove = new ArrayList<>();
		typeDeclarations = new ArrayList<>();
	}

	@Override
	public boolean visit( ClassInstanceCreation node )
	{
		Type type = node.getType();
		boolean target = ASTUtil.isClassOf(node, SwingWorkerInfo.getBinaryName());
		if ( target && type instanceof ParameterizedType )
		{
			List argumentTypes = ( (ParameterizedType) type ).typeArguments();
			resultType = (Type) argumentTypes.get( 0 );
			processType = (Type) argumentTypes.get( 1 );
		}
		return true;
	}

	@Override
	public boolean visit( Block node )
	{
		ASTNode parent = node.getParent();
		if ( parent instanceof MethodDeclaration )
		{
			MethodDeclaration methodDeclaration = (MethodDeclaration) parent;
			String methodDeclarationName = methodDeclaration.getName().toString();
			if ( DO_IN_BACKGROUND.equals( methodDeclarationName ) )
			{
				doInBackgroundBlock = node;
			}
			else if ( DONE.equals( methodDeclarationName ) )
			{
				doneBlock = node;
			}
			else if ( PROCESS.equals( methodDeclarationName ) )
			{
				processBlock = node;
				processVariableName = ASTUtil.getVariableName( methodDeclaration, 0 );
			}
		}
		return true;
	}

	@Override
	public boolean visit( TypeDeclaration node )
	{
		typeDeclarations.add( node );
		return true;
	}

	@Override
	public boolean visit( FieldDeclaration node )
	{
		TypeDeclaration parent = ASTUtil.findParent( node, TypeDeclaration.class );
		if ( isRelevant( parent ) )
		{
			fieldDeclarations.add( node );
		}
		return true;
	}

	@Override
	public boolean visit( MethodDeclaration node )
	{
		if ( constructor == null && node.isConstructor() )
		{
			constructor = node;
		}
		else
		{
			allMethodDeclarations.add( node );
		}

		TypeDeclaration parent = ASTUtil.findParent( node, TypeDeclaration.class );
		if ( isRelevant( parent ) )
		{
			String methodDeclarationName = node.getName().toString();
			if ( !DO_IN_BACKGROUND.equals( methodDeclarationName ) &&
					!DONE.equals( methodDeclarationName ) &&
					!PROCESS.equals( methodDeclarationName ) )
			{
				additionalMethodDeclarations.add( node );
			}
		}
		return true;
	}

	@Override
	public boolean visit( SuperMethodInvocation node )
	{
		if ( ASTUtil.matchesTargetMethod( node, DONE, SwingWorkerInfo.getBinaryName() ) ||
				ASTUtil.matchesTargetMethod( node, PROCESS, SwingWorkerInfo.getBinaryName() ) )
		{
			superMethodInvocationsToRemove.add( node );
		}
		return true;
	}

	public Block getDoInBackgroundBlock()
	{
		return doInBackgroundBlock;
	}

	public Block getDoneBlock()
	{
		return doneBlock;
	}

	public Type getResultType()
	{
		return resultType;
	}

	public Type getProcessType()
	{
		return processType;
	}

	public Block getProcessBlock()
	{
		return processBlock;
	}

	public String getProcessVariableName()
	{
		return processVariableName;
	}

	public List<FieldDeclaration> getFieldDeclarations()
	{
		return fieldDeclarations;
	}

	public List<MethodDeclaration> getAdditionalMethodDeclarations()
	{
		return additionalMethodDeclarations;
	}

	public List<SuperMethodInvocation> getSuperMethodInvocationsToRemove()
	{
		return superMethodInvocationsToRemove;
	}

	public List<TypeDeclaration> getTypeDeclarations()
	{
		return typeDeclarations;
	}

	public MethodDeclaration getConstructor()
	{
		return constructor;
	}

	public List<MethodDeclaration> getAllMethodDeclarations()
	{
		return allMethodDeclarations;
	}

	public boolean hasAdditionalFieldsOrMethods()
	{
		return !fieldDeclarations.isEmpty() ||
				!additionalMethodDeclarations.isEmpty() ||
				!typeDeclarations.isEmpty();
	}

	// ### Private Methods ###

	private boolean isRelevant( TypeDeclaration parent )
	{
		boolean ignore = false;
		for ( TypeDeclaration typeDeclaration : typeDeclarations )
		{
			if ( typeDeclaration.equals( parent ) )
			{
				ignore = true;
				break;
			}
		}
		return !ignore;
	}
}
