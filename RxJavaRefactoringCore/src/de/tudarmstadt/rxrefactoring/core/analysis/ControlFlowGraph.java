package de.tudarmstadt.rxrefactoring.core.analysis;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.AbstractBaseGraph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.ObjectArrays;

import de.tudarmstadt.rxrefactoring.core.logging.Log;
import de.tudarmstadt.rxrefactoring.core.utils.Statements;

public class ControlFlowGraph extends AbstractBaseGraph<Statement, Edge<Statement>>
		implements DirectedGraph<Statement, Edge<Statement>> {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 4664573166055105543L;
	

	protected ControlFlowGraph() {
		super((v1, v2) -> new StatementEdge(v1, v2), false, true);
	}

	
	static interface StatementNode {
		public Statement getStatement();
		public boolean isStart();
		public boolean isEnd();		
	}
	
	/**
	 * Creates a CFG from a statement block.
	 * 
	 * @param block
	 * @return
	 */
	public static ControlFlowGraph from(Statement statement) {
		ControlFlowGraph graph = new ControlFlowGraph();
		GraphBuildingUtils.from(graph, statement);
		return graph;
	}


	
	
	/**
	 * This class contains the functionality to generate a control flow graph
	 * from a statement.
	 * 
	 * @author mirko
	 *
	 */
	private static class GraphBuildingUtils {
		
		private static class BlockContext {
			/**
			 * The enclosing blocks in the current call context.
			 */
			private final Statement[] callBlocks;
			
			/**
			 * The list of statements in the current block. Empty, if not currently in a block
			 * or the block has no statements.
			 */
			private final List<?> statements;
						
			public BlockContext(List<?> statements, Statement... callBlocks) {
				this.statements = statements;
				this.callBlocks = callBlocks;
			}
			
			public static BlockContext empty() {
				return new BlockContext(Collections.EMPTY_LIST);
			}
			
			public Statement getStatementAt(int index) {
				return (Statement) statements.get(index);
			}
			
			public int numberOfStatements() {
				return statements.size();
			}
			
			public BlockContext enterBlock(Block block) {
				return new BlockContext(block.statements(), ObjectArrays.concat(block, callBlocks));
			}
			
			public BlockContext enterControlFlowStatement(Statement statement) {
				return new BlockContext(Collections.EMPTY_LIST, ObjectArrays.concat(statement, callBlocks));
			}
			
			public Statement enclosingLoop() {								
				return enclosingLoop(null);			
			}
			
			/**
			 * Finds the enclosing loop of this context that has a specific label name.
			 * @param label The label of the enclosing loop, or null if the next enclosing loop regardless of the label should be found.
			 * @return The enclosing loop of this context, or null if there is no loop.
			 */
			public Statement enclosingLoop(SimpleName label) {
								
				for (Object element : callBlocks) {
					
					Statement statement = (Statement) element;
					
					if (Statements.isLoop(statement) && label == null 
						|| statement instanceof LabeledStatement && Objects.equals(label.getIdentifier(), ((LabeledStatement) statement).getLabel().getIdentifier())) {
						return statement;
					}
				}
				
				return null;
				
			}
			
			
			
			
			@Override
			public String toString() {
				return "{ call hierarchy=" + Arrays.toString(callBlocks) + ", current=" + statements + " }";
			}
		}
		
		public static void from(ControlFlowGraph graph, Statement statement) {
			GraphBuildingUtils.from(graph, BlockContext.empty(), -1, statement);
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
		private static Statement[] from(ControlFlowGraph graph, BlockContext context, int currentIndex,
				Statement currentStatement, Statement... previousStatements) {

			//Decide how to traverse further by looking at the current statement.
			//There should be a case for each statement that changes the control flow.			
			if (currentStatement instanceof Block) {
				//If we look at a block, discard the block and only look at the statements in the block.
				Block block = (Block) currentStatement;

								//If there are statements in the block, move to the first statement of the block with 
				//the previous statements of the block as previous statements of the first statement
				if (block.statements().size() > 0) {					
					BlockContext newContext = context.enterBlock(block);
					return from(graph, newContext, 0, newContext.getStatementAt(0), previousStatements);
				//If there are no statements in the block, completely ignore the block.
				} else {
					return previousStatements;
				}
					
			} else if (currentStatement instanceof LabeledStatement) {
				LabeledStatement labeledStatement = (LabeledStatement) currentStatement;
				
				//Add a vertex of the current statement to the graph.	
				graph.addVertex(currentStatement);
				addEdges(graph, currentStatement, previousStatements);
				
				Statement[] exits = from(graph, context.enterControlFlowStatement(currentStatement), -1, labeledStatement.getBody(), currentStatement);
				
				//Ignore the labeled statement for now
				return moveToNextStatement(graph, context, currentIndex, exits);
				
			} else if (currentStatement instanceof IfStatement) {
				IfStatement ifStatement = (IfStatement) currentStatement;
				
				//Add a vertex of the current statement to the graph.	
				graph.addVertex(ifStatement);
				addEdges(graph, ifStatement, previousStatements);
				
				
				Statement thenStatement = ifStatement.getThenStatement();
				Statement elseStatement = ifStatement.getElseStatement();

				//Compute the exit entries for an if statement depending on whether there is an else branch
				Statement[] exits;
				if (elseStatement == null) {
					//If there is no else branch, then either the if statement or the last statement of the then-branch
					//can be used as previous statements
					exits = ObjectArrays.concat(ifStatement,
							from(graph, context.enterControlFlowStatement(ifStatement), -1, thenStatement, ifStatement));
				} else {
					//If there is an else branch, then either the last statement of the then-branch
					//or the last statement of the else-branch can be used as previous statements
					exits = ObjectArrays.concat(from(graph, context.enterControlFlowStatement(ifStatement), -1, thenStatement, ifStatement),
							from(graph, context.enterControlFlowStatement(ifStatement), -1, elseStatement, ifStatement), Statement.class);
				}
				
				//Move to the next statement with the last statement of either branch.
				return moveToNextStatement(graph, context, currentIndex, exits);
				
			} else if (currentStatement instanceof WhileStatement) {
				
				//Add a vertex of the current statement to the graph.	
				graph.addVertex(currentStatement);
				addEdges(graph, currentStatement, previousStatements);
				
				WhileStatement whileStatement = (WhileStatement) currentStatement;
				Statement bodyStatement = whileStatement.getBody();
				
				//Create edges inside while-block with while as previous edge
				Statement[] whileExits = from(graph, context.enterControlFlowStatement(whileStatement), -1, bodyStatement, whileStatement);
				
				//Add an edge from the end of the while block to the beginning of the while block
				addEdges(graph, whileStatement, whileExits);
							
				//Move to the next statement in the main block
				return moveToNextStatement(graph, context, currentIndex, currentStatement);				
			
			} else if (currentStatement instanceof DoStatement) {
				
				//Add a vertex of the current statement to the graph.	
				graph.addVertex(currentStatement);
				
				DoStatement doStatement = (DoStatement) currentStatement;
				Statement bodyStatement = doStatement.getBody();
				
				//Create edges inside while-block with while as previous edge
				Statement[] doExits = from(graph, context.enterControlFlowStatement(doStatement), -1, bodyStatement, ObjectArrays.concat(previousStatements, doStatement));
				
				//Add an edge from the end of the while block to the beginning of the while block
				addEdges(graph, doStatement, doExits);
				
				//Move to the next statement in the main block
				return moveToNextStatement(graph, context, currentIndex, currentStatement);
				
			} else if (currentStatement instanceof ForStatement) {
				
				//Add a vertex of the current statement to the graph.	
				graph.addVertex(currentStatement);
				addEdges(graph, currentStatement, previousStatements);
				
				ForStatement forStatement = (ForStatement) currentStatement;
				Statement bodyStatement = forStatement.getBody();
				
				//Create edges inside for-block with while as previous edge
				Statement[] forExits = from(graph, context.enterControlFlowStatement(forStatement), -1, bodyStatement, forStatement);
				
				//Add an edge from the end of the for-block to the beginning of the for-block
				addEdges(graph, forStatement, forExits);
							
				//Move to the next statement in the main block
				return moveToNextStatement(graph, context, currentIndex, currentStatement);				
			
			} else if (currentStatement instanceof ContinueStatement) {
				ContinueStatement continueStatement = (ContinueStatement) currentStatement;
				
				//Add a vertex of the current statement to the graph.	
				graph.addVertex(continueStatement);
				addEdges(graph, continueStatement, previousStatements);
								
				Statement enclosingLoop = context.enclosingLoop(continueStatement.getLabel());
				if (enclosingLoop != null) { //Enclosing loop should never be null as continue can only be used in loops.
					addEdges(graph, enclosingLoop, continueStatement);	
				}
				
				//Stop traversion of the block.
				return new Statement[] { };
			} else if (currentStatement instanceof BreakStatement) {
				//TODO: Check Break statements!!!
				
				//Add a vertex of the current statement to the graph.	
				graph.addVertex(currentStatement);
				addEdges(graph, currentStatement, previousStatements);
				
				//Stop traversion of the block.
				return new Statement[] { currentStatement };
			} else if (currentStatement instanceof ReturnStatement) {
				
				//Add a vertex of the current statement to the graph.	
				graph.addVertex(currentStatement);
				addEdges(graph, currentStatement, previousStatements);
				
				//Do not traverse further.
				return new Statement[] { };
			} else {
				//If the statement does not affect the control flow, then just go to the next statement.
				
				//Add a vertex of the current statement to the graph.	
				graph.addVertex(currentStatement);
				addEdges(graph, currentStatement, previousStatements);
								
				return moveToNextStatement(graph, context, currentIndex, currentStatement);
			}

		}

		private static Statement[] moveToNextStatement(ControlFlowGraph graph, BlockContext context, int currentIndex,
				Statement... useAsPreviousStatements) {
			
			if (currentIndex < context.numberOfStatements() - 1) {
				return from(graph, context, currentIndex + 1, context.getStatementAt(currentIndex + 1),
						useAsPreviousStatements);
			} else {
				return useAsPreviousStatements;
			}
		}
		
		
		
		
		
		
		
		
		
		
		private class ExitStatements {
			private final Multimap<Statement, Statement> exits = HashMultimap.create();
			
			public ExitStatements() {
				
			}
			
			public Collection<Statement> get(Statement statement) {
				return exits.get(statement);
			}
			
		}
		
		
		
		
		
		private static ExitStatements from2(ControlFlowGraph graph, BlockContext context, Statement currentStatement) {

			//Decide how to traverse further by looking at the current statement.
			//There should be a case for each statement that changes the control flow.			
			if (currentStatement instanceof Block) {
				//If we look at a block, discard the block and only look at the statements in the block.
				Block block = (Block) currentStatement;
				
				Collection<Statement> previousStatements = Collections.singleton(block);
				
				Multimap<Statement, Statement> result = HashMultimap.create();
				
				for (Object element : block.statements()) {
					Statement statement = (Statement) element;
					
					//Add a vertex of the current statement to the graph.	
					graph.addVertex(statement);
					addEdges(graph, statement, previousStatements);
					
					ExitStatements exits = from2(graph, context, statement);					
					for (Statement exit : exits.get(block) ) {
						result.
					}
					
				}
				
								//If there are statements in the block, move to the first statement of the block with 
				//the previous statements of the block as previous statements of the first statement
				if (block.statements().size() > 0) {					
					BlockContext newContext = context.enterBlock(block);
					return from(graph, newContext, 0, newContext.getStatementAt(0), previousStatements);
				//If there are no statements in the block, completely ignore the block.
				} else {
					return previousStatements;
				}
					
//			} else if (currentStatement instanceof LabeledStatement) {
//				LabeledStatement labeledStatement = (LabeledStatement) currentStatement;
//				
//				//Add a vertex of the current statement to the graph.	
//				graph.addVertex(currentStatement);
//				addEdges(graph, currentStatement, previousStatements);
//				
//				Statement[] exits = from(graph, context.enterControlFlowStatement(currentStatement), -1, labeledStatement.getBody(), currentStatement);
//				
//				//Ignore the labeled statement for now
//				return moveToNextStatement(graph, context, currentIndex, exits);
//				
//			} else if (currentStatement instanceof IfStatement) {
//				IfStatement ifStatement = (IfStatement) currentStatement;
//				
//				//Add a vertex of the current statement to the graph.	
//				graph.addVertex(ifStatement);
//				addEdges(graph, ifStatement, previousStatements);
//				
//				
//				Statement thenStatement = ifStatement.getThenStatement();
//				Statement elseStatement = ifStatement.getElseStatement();
//
//				//Compute the exit entries for an if statement depending on whether there is an else branch
//				Statement[] exits;
//				if (elseStatement == null) {
//					//If there is no else branch, then either the if statement or the last statement of the then-branch
//					//can be used as previous statements
//					exits = ObjectArrays.concat(ifStatement,
//							from(graph, context.enterControlFlowStatement(ifStatement), -1, thenStatement, ifStatement));
//				} else {
//					//If there is an else branch, then either the last statement of the then-branch
//					//or the last statement of the else-branch can be used as previous statements
//					exits = ObjectArrays.concat(from(graph, context.enterControlFlowStatement(ifStatement), -1, thenStatement, ifStatement),
//							from(graph, context.enterControlFlowStatement(ifStatement), -1, elseStatement, ifStatement), Statement.class);
//				}
//				
//				//Move to the next statement with the last statement of either branch.
//				return moveToNextStatement(graph, context, currentIndex, exits);
//				
//			} else if (currentStatement instanceof WhileStatement) {
//				
//				//Add a vertex of the current statement to the graph.	
//				graph.addVertex(currentStatement);
//				addEdges(graph, currentStatement, previousStatements);
//				
//				WhileStatement whileStatement = (WhileStatement) currentStatement;
//				Statement bodyStatement = whileStatement.getBody();
//				
//				//Create edges inside while-block with while as previous edge
//				Statement[] whileExits = from(graph, context.enterControlFlowStatement(whileStatement), -1, bodyStatement, whileStatement);
//				
//				//Add an edge from the end of the while block to the beginning of the while block
//				addEdges(graph, whileStatement, whileExits);
//							
//				//Move to the next statement in the main block
//				return moveToNextStatement(graph, context, currentIndex, currentStatement);				
//			
//			} else if (currentStatement instanceof DoStatement) {
//				
//				//Add a vertex of the current statement to the graph.	
//				graph.addVertex(currentStatement);
//				
//				DoStatement doStatement = (DoStatement) currentStatement;
//				Statement bodyStatement = doStatement.getBody();
//				
//				//Create edges inside while-block with while as previous edge
//				Statement[] doExits = from(graph, context.enterControlFlowStatement(doStatement), -1, bodyStatement, ObjectArrays.concat(previousStatements, doStatement));
//				
//				//Add an edge from the end of the while block to the beginning of the while block
//				addEdges(graph, doStatement, doExits);
//				
//				//Move to the next statement in the main block
//				return moveToNextStatement(graph, context, currentIndex, currentStatement);
//				
//			} else if (currentStatement instanceof ForStatement) {
//				
//				//Add a vertex of the current statement to the graph.	
//				graph.addVertex(currentStatement);
//				addEdges(graph, currentStatement, previousStatements);
//				
//				ForStatement forStatement = (ForStatement) currentStatement;
//				Statement bodyStatement = forStatement.getBody();
//				
//				//Create edges inside for-block with while as previous edge
//				Statement[] forExits = from(graph, context.enterControlFlowStatement(forStatement), -1, bodyStatement, forStatement);
//				
//				//Add an edge from the end of the for-block to the beginning of the for-block
//				addEdges(graph, forStatement, forExits);
//							
//				//Move to the next statement in the main block
//				return moveToNextStatement(graph, context, currentIndex, currentStatement);				
//			
//			} else if (currentStatement instanceof ContinueStatement) {
//				ContinueStatement continueStatement = (ContinueStatement) currentStatement;
//				
//				//Add a vertex of the current statement to the graph.	
//				graph.addVertex(continueStatement);
//				addEdges(graph, continueStatement, previousStatements);
//								
//				Statement enclosingLoop = context.enclosingLoop(continueStatement.getLabel());
//				if (enclosingLoop != null) { //Enclosing loop should never be null as continue can only be used in loops.
//					addEdges(graph, enclosingLoop, continueStatement);	
//				}
//				
//				//Stop traversion of the block.
//				return new Statement[] { };
//			} else if (currentStatement instanceof BreakStatement) {
//				//TODO: Check Break statements!!!
//				
//				//Add a vertex of the current statement to the graph.	
//				graph.addVertex(currentStatement);
//				addEdges(graph, currentStatement, previousStatements);
//				
//				//Stop traversion of the block.
//				return new Statement[] { currentStatement };
//			} else if (currentStatement instanceof ReturnStatement) {
//				
//				//Add a vertex of the current statement to the graph.	
//				graph.addVertex(currentStatement);
//				addEdges(graph, currentStatement, previousStatements);
//				
//				//Do not traverse further.
//				return new Statement[] { };
			} else {
				//If the statement does not affect the control flow, then just go to the next statement.
				
				
				graph.addVertex(currentStatement);
				addEdges(graph, currentStatement, previousStatements);
								
				return moveToNextStatement(graph, context, currentIndex, currentStatement);
			}

		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		private static void addEdges(ControlFlowGraph graph, Statement currentStatement, Statement... previousStatements) {
			for (Statement previousStatement : previousStatements) {
				//Add an edge previousStatement --> currentStatement
				Edge<Statement> edge = graph.addEdge(previousStatement, currentStatement);
				Log.info(ControlFlowGraph.class, "Add edge : " + edge);
			}			
		}
		
		private static void addEdges(ControlFlowGraph graph, Statement currentStatement, Iterable<Statement> previousStatements) {
			for (Statement previousStatement : previousStatements) {
				//Add an edge previousStatement --> currentStatement
				Edge<Statement> edge = graph.addEdge(previousStatement, currentStatement);
				Log.info(ControlFlowGraph.class, "Add edge : " + edge);
			}			
		}
		
		
	}
	
	
	
	public String listEdges() {
		String result = "";
		for (Edge<Statement> edge : edgeSet()) {
			result += edge.toString() + ";\n";
		}
		return result;
	}

}
