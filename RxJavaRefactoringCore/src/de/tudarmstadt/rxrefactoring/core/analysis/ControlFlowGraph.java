package de.tudarmstadt.rxrefactoring.core.analysis;

import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.AbstractBaseGraph;

import com.google.common.collect.ObjectArrays;

import de.tudarmstadt.rxrefactoring.core.logging.Log;

public class ControlFlowGraph extends AbstractBaseGraph<Statement, Edge<Statement>>
		implements DirectedGraph<Statement, Edge<Statement>> {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 4664573166055105543L;

	protected ControlFlowGraph() {
		super((v1, v2) -> new StatementEdge(v1, v2), false, true);
	}

	/**
	 * Creates a CFG from a statement block.
	 * 
	 * @param block
	 * @return
	 */
	public static ControlFlowGraph from(Block block) {
		ControlFlowGraph graph = new ControlFlowGraph();
		from(graph, null, 0, block);
		return graph;
	}

	/**
	 * Builds a control flow graph from a given statement.
	 * 
	 * @param graph
	 *            The graph that should be changed.
	 * @param staments
	 *            The current list of statements to work on, i.e. the statements in
	 *            a block.
	 * @param currentIndex
	 *            The index of the currently looked statement.
	 * @param currentStatement
	 *            The statement that is currently looked on.
	 * @param previousStatements
	 *            The previous statements.
	 * 
	 * @return The last statement(s) that has been processed in a basic block.
	 */
	private static Statement[] from(ControlFlowGraph graph, List statements, int currentIndex,
			Statement currentStatement, Statement... previousStatements) {

		//If we look at a block, discard the block and only look at the statements in the block.
		if (currentStatement instanceof Block) {
			Block block = (Block) currentStatement;

			//If there are statements in the block, move to the first statement of the block with 
			//the previous statements of the block as previous statements of the first statement
			if (block.statements().size() > 0)
				return from(graph, block.statements(), 0, (Statement) block.statements().get(0), previousStatements);
			//If there are no statements in the block, completely ignore the block.
			else
				return previousStatements;
		}

		//Got to this part only if !(currentStatement instanceof Block)
		
		//Add a vertex of the current statement to the graph.
		graph.addVertex(currentStatement);
		for (Statement previousStatement : previousStatements) {
			//Add an edge previousStatement --> currentStatement
			Edge<Statement> edge = graph.addEdge(previousStatement, currentStatement);
			Log.info(ControlFlowGraph.class, "Add edge : " + edge);
		}

		//Decide how to traverse further by looking at the current statement.
		
		if (currentStatement instanceof IfStatement) {			
			IfStatement ifStatement = (IfStatement) currentStatement;
			Statement thenStatement = ifStatement.getThenStatement();
			Statement elseStatement = ifStatement.getElseStatement();

			//Compute the exit entries for an if statement depending on whether there is an else branch
			Statement[] exits;
			if (elseStatement == null) {
				//If there is no else branch, then either the if statement or the last statement of the then-branch
				//can be used as previous statements
				exits = ObjectArrays.concat(ifStatement,
						from(graph, statements, currentIndex, thenStatement, ifStatement));
			} else {
				//If there is an else branch, then either the last statement of the then-branch
				//or the last statement of the else-branch can be used as previous statements
				exits = ObjectArrays.concat(from(graph, statements, currentIndex, thenStatement, ifStatement),
						from(graph, statements, currentIndex, elseStatement, ifStatement), Statement.class);
			}
			
			//Move to the next statement with the last statement of either branch.
			return moveToNextStatement(graph, statements, currentIndex, exits);
			
		} else if (currentStatement instanceof ReturnStatement) {
			//Do not traverse further.
			return new Statement[] {currentStatement};
		} else {
			//If the statement does not affect the control flow, then just go to the next statement.
			return moveToNextStatement(graph, statements, currentIndex, currentStatement);
		}

	}

	private static Statement[] moveToNextStatement(ControlFlowGraph graph, List statements, int currentIndex,
			Statement... useAsPreviousStatements) {
		if (currentIndex < statements.size() - 1) {
			return from(graph, statements, currentIndex + 1, (Statement) statements.get(currentIndex + 1),
					useAsPreviousStatements);
		} else {
			return useAsPreviousStatements;
		}
	}

}
