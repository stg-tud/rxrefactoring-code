package rxjavarefactoring.analyzers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/25/2016
 */
public class UsagesTreeNode<CurrentNode extends ASTNode>
{
	private int level;
	private UsagesTreeNode predecessor;
	private CurrentNode node;
	private List<UsagesTreeNode<? extends ASTNode>> children;

	public UsagesTreeNode( CurrentNode node )
	{
		this.level = 0;
		this.node = node;
		children = new ArrayList<>();
	}

	public void addChild( UsagesTreeNode<? extends ASTNode> child )
	{
		child.level = level + 1;
		child.predecessor = this;
		children.add( child );
	}

	public UsagesTreeNode getPredecessor()
	{
		return predecessor;
	}

	public CurrentNode getNode()
	{
		return node;
	}

	public List<UsagesTreeNode<? extends ASTNode>> getChildren()
	{
		return children;
	}

	@Override
	public String toString()
	{
		return toString( this );
	}

	private String toString( UsagesTreeNode<CurrentNode> node )
	{
		String level = new String( new char[ node.level ] ).replace( '\0', '-' );
		level = node.level + "" + level;
		if ( node.children.isEmpty() )
		{
			return level + "> " + getString( node );
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append( level );
			sb.append( "> " );
			sb.append( getString( node ) );
			sb.append( "\n" );
			int counter = 0;
			for ( UsagesTreeNode child : node.children )
			{
				counter++;
				sb.append( toString( child ) );
				if ( counter < node.children.size() )
				{
					sb.append( "\n" );
				}
			}
			return sb.toString();
		}
	}

	private String getString( UsagesTreeNode<CurrentNode> node )
	{
		if ( node.node == null )
		{
			return "Project";
		}
		if ( node.node instanceof ClassInstanceCreation )
		{
			String instanceCreation = ( (ClassInstanceCreation) node.node ).getType().toString();
			return "Instance creation: " + instanceCreation;
		}

		if ( node.node instanceof TypeDeclaration )
		{
			String typeDeclaration = ( (TypeDeclaration) node.node ).getName().toString();
			return "Type: " + typeDeclaration;
		}

		if ( node.node instanceof VariableDeclaration )
		{
			String varDecl = ( (VariableDeclaration) node.node ).getName().toString();
			return "Variable: " + varDecl;
		}

		if ( node.node instanceof MethodDeclaration )
		{
			MethodDeclaration methodDeclaration = (MethodDeclaration) node.node;
			String methodDec = methodDeclaration.getName().toString();
			String declaringClass = methodDeclaration.resolveBinding().getDeclaringClass().getName();
			return "Method declaration: " + declaringClass + "#" + methodDec;
		}

		if ( node.node instanceof MethodInvocation )
		{
			String methodInv = ( (MethodInvocation) node.node ).getName().toString();
			return "Method invocation: " + methodInv;
		}

		if ( node.node instanceof SingleVariableDeclaration )
		{
			String singleVarDecl = ( (SingleVariableDeclaration) node.node ).getName().toString();
			return "Var in method decl: " + singleVarDecl;
		}
		return "not defined! ->" + node.node.toString();
	}
}
