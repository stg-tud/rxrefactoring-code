package de.tudarmstadt.rxrefactoring.core.analysis.cfg.statement;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.jgrapht.graph.AbstractBaseGraph;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IEdge;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.SimpleEdge;

/**
 * This class models the intraprocedural control flow graph of
 * a statement, e.g. a method body.
 * 
 * @author mirko
 *
 */
public class ProgramGraph extends AbstractBaseGraph<ASTNode, IEdge<ASTNode>>
		implements IControlFlowGraph<ASTNode> {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 4664573166055105543L;
	
	private final Set<ASTNode> entryNodes;
	private final Set<ASTNode> exitNodes;	

	/**
	 * Use {@link ProgramGraph#createFrom(Statement)} to correctly set all attributes of the
	 * program graph.
	 */
	protected ProgramGraph() {
		super((v1, v2) -> new SimpleEdge<>(v1, v2), false, true);	
		entryNodes = Sets.newHashSet();
		exitNodes = Sets.newHashSet();
	}	
	
	/**
	 * Creates an intraprocedural control flow graph from a program (i.e. a statement, for example
	 * a method body).
	 * 
	 * @param statement The statement from which the graph is created.
	 * @return A control flow graph of the statement.
	 * 
	 * @see ProgramGraphBuilder#process(Statement)
	 */
	public static ProgramGraph createFrom(Statement statement) {
		ProgramGraph graph = new ProgramGraph();
		
		if (statement != null)
			graph.addEntryNode(statement);
		
		ProgramGraphBuilder builder = new ProgramGraphBuilder(graph);
		builder.process(statement);
		return graph;
	}		

	void addEntryNode(ASTNode node) {
		entryNodes.add(node);
	}
	
	void addExitNode(ASTNode node) {
		exitNodes.add(node);
	}
	
	boolean hasExitNodes() {
		return !exitNodes.isEmpty();
	}
	
	@Override
	public Set<ASTNode> entryNodes() {
		return entryNodes;		
	}
	
	public Set<ASTNode> exitNodes() {
		return exitNodes;		
	}
	
	
	
	public String listEdges() {
		String result = "";
		for (IEdge<ASTNode> edge : edgeSet()) {
			result += edge.toString() + ";\n";
		}
		return result;
	}

}
