package rxjavarefactoring.analyzers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/25/2016
 */
public class UsagesTreeNode<Predecessor extends ASTNode, CurrentNode extends ASTNode>
{
	private Predecessor predecessor;
	private CurrentNode node;
	private List<UsagesTreeNode<CurrentNode, ? extends ASTNode>> children;

	public UsagesTreeNode( Predecessor predecessor, CurrentNode node )
	{
		this.predecessor = predecessor;
		this.node = node;
		children = new ArrayList<>();
	}

	public void addChild( UsagesTreeNode<CurrentNode, ? extends ASTNode> child )
	{
		children.add( child );
	}

	public Predecessor getPredecessor()
	{
		return predecessor;
	}

	public CurrentNode getNode()
	{
		return node;
	}

	public List<UsagesTreeNode<CurrentNode, ? extends ASTNode>> getChildren()
	{
		return children;
	}

	@Override
	public String toString()
	{
		return toString( this );
	}

	private String toString( UsagesTreeNode<Predecessor, CurrentNode> node )
	{
		if ( node.children.isEmpty() )
		{
			return getString( node );
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append( getString( node ) );
			sb.append( " -> [" );
			int counter = 0;
			for ( UsagesTreeNode child : node.children )
			{
				counter++;
				sb.append( toString( child ) );
				if ( counter < node.children.size() )
				{
					sb.append( ", " );
				}
			}
			sb.append( "]" );
			return sb.toString();
		}
	}

	private String getString( UsagesTreeNode<Predecessor, CurrentNode> node )
	{
		if ( node.node == null )
		{
			return "root";
		}
		if ( node.node instanceof ClassInstanceCreation )
		{
			return ( (ClassInstanceCreation) node.node ).getType().toString();
		}

		if ( node.node instanceof TypeDeclaration )
		{
			return ( (TypeDeclaration) node.node ).getName().toString();
		}

		if ( node.node instanceof VariableDeclaration )
		{
			return ( (VariableDeclaration) node.node ).getName().toString();
		}

		if ( node.node instanceof MethodDeclaration )
		{
			return ( (MethodDeclaration) node.node ).getName().toString();
		}

		if ( node.node instanceof MethodInvocation )
		{
			return ( (MethodInvocation) node.node ).getName().toString();
		}

		if ( node.node instanceof SingleVariableDeclaration )
		{
			return ( (SingleVariableDeclaration) node.node ).getName().toString();
		}
		return "not defined! ->" + node.node.toString();
	}
}
