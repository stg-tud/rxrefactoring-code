package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.AbstractBaseGraph;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.utils.Statements;

//TODO: Add Exceptions to the Control Flow Graph
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


	public Set<Statement> entryNodes() {
		return vertexSet().stream().filter(stmt -> inDegreeOf(stmt) == 0).collect(Collectors.toSet());		
	}
	
	public Set<Statement> exitNodes() {
		return vertexSet().stream().filter(stmt -> outDegreeOf(stmt) == 0).collect(Collectors.toSet());		
	}
	
	
	
	/**
	 * This class contains the functionality to generate a control flow graph
	 * from a statement.
	 * 
	 * @author mirko
	 *
	 */
	private static class GraphBuildingUtils {
		
		/**
		 * The context defines the nesting of classes.
		 * 
		 * @author mirko
		 *
		 */
		private static class Context {
			/**
			 * The enclosing blocks in the current call context.
			 */
			private final Statement[] callBlocks;
			
						
			public Context(Statement... callBlocks) {
				this.callBlocks = callBlocks;
			}
			
			public static Context empty() {
				return new Context();
			}
			
			
			public Context enterStatement(Statement statement) {
				return new Context(ObjectArrays.concat(statement, callBlocks));
			}
			
				
			/**
			 * Finds the enclosing statement that a continue statement refers to.
			 * 
			 * @param label The label of the continue statement, or null if there is no label.
			 * @return The enclosing statement that is referred to by a continue in this context.
			 */
			public Statement enclosingContinueBlock(SimpleName label) {
								
				for (Object element : callBlocks) {
					
					Statement statement = (Statement) element;
					
					if (Statements.isLoop(statement) && label == null 
						|| statement instanceof LabeledStatement 
							&& Statements.isLoop(((LabeledStatement) statement).getBody()) 
							&& Objects.equals(label.getIdentifier(), ((LabeledStatement) statement).getLabel().getIdentifier())) {
						return statement;
					}
				}
				
				return null;				
			}
			
			public Statement enclosingBreakBlock(SimpleName label) {
				
				for (Object element : callBlocks) {
					
					Statement statement = (Statement) element;
					
					if (label == null && (Statements.isLoop(statement) || statement instanceof SwitchStatement) 
						|| statement instanceof LabeledStatement && Objects.equals(label.getIdentifier(), ((LabeledStatement) statement).getLabel().getIdentifier())
					) {
						return statement;
					}
				}
				
				return null;
			}
			
			public SwitchStatement enclosingSwitch() {
				for (Object element : callBlocks) {					
					if (element instanceof SwitchStatement) {
						return (SwitchStatement) element;
					}
				}
				
				return null;
			}
			
			
			
			
			@Override
			public String toString() {
				return "Context" + Arrays.toString(callBlocks);
			}
		}
		
		public static void from(ControlFlowGraph graph, Statement statement) {
			GraphBuildingUtils.from(graph, Context.empty(), statement);
		}
		
		private static class FlowResult {
			/**
			 * Multimap from statement to all additional exits that should be added to the CFG node of that statement.
			 */
			private final Multimap<Statement, Statement> exits;
			
			
			private FlowResult(Multimap<Statement, Statement> exits) {
				this.exits = exits;
			}
			
			private FlowResult() {
				this(newMultimap());
			}
			
			/**
			 * Adds results of a child computation to the flow result.
			 * 
			 * @param currentStatement The statement where to put the exits.
			 * @param additionalStatements Statements that should be put as exit.
			 * @param childStatements The child statements which are removed from their results.
			 * @param childResults The child results that are used for merging.
			 * @param addChildExits True, if the exits of the children should be added to the new result.
			 */
			protected void addTo(Statement currentStatement, Statement[] additionalStatements, Statement[] childStatements, FlowResult[] childResults, boolean addChildExits) {
				for (FlowResult childResult : childResults) {
					exits.putAll(childResult.exits);
				}				
				
				for (Statement statement : childStatements) {
					if (addChildExits) 
						exits.putAll(currentStatement, exits.get(statement));
					exits.removeAll(statement);
				}
				
				for (Statement statement : additionalStatements) {
					exits.put(currentStatement, statement);
				}
			}
			
								
			private static Multimap<Statement, Statement> newMultimap() {
				return Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
			}
			
			public static FlowResult create(Statement... statements) {
				Multimap<Statement, Statement> exits = newMultimap();
				for (Statement statement : statements) {
					exits.put(statement, statement);
				}
				return new FlowResult(exits);
			}
			
			
			public static FlowResult concat(Statement currentStatement, Statement[] additionalStatements, Statement[] childStatements, FlowResult[] childResults, boolean addChildExits) {
				
				FlowResult result = new FlowResult();
				result.addTo(currentStatement, additionalStatements, childStatements, childResults, addChildExits);				
				
				return result;
			}
			
			@Override
			public String toString() {
				return "FlowResult(" + exits.toString() + ")";
			}
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
		private static FlowResult from(ControlFlowGraph graph, Context context, Statement currentStatement) {

			//Decide how to traverse further by looking at the current statement.
			//There should be a case for each statement that changes the control flow.			
			if (currentStatement instanceof Block) {
				//If we look at a block, discard the block and only look at the statements in the block.
				Block block = (Block) currentStatement;
				Collection<Statement> previousStatements = Collections.singleton(block);
				
				Context newContext = context.enterStatement(block);
				
				FlowResult result = FlowResult.create(); 
				
				//Iterate through all statements in the block and establish links between them.
				for (Object element : block.statements()) {
					Statement statement = (Statement) element;
					
					addEdges(graph, previousStatements, statement);
					
					FlowResult statementResult = from(graph, newContext, statement);
					result.addTo(statement, new Statement[0], new Statement[] {statement}, new FlowResult[] {statementResult}, true);				
					
					previousStatements = statementResult.exits.get(statement); 					
				}
				
				result.exits.putAll(block, previousStatements);
				
				return result;
				
			} else if (currentStatement instanceof LabeledStatement) {
				LabeledStatement labeledStatement = (LabeledStatement) currentStatement;
				
				Statement bodyStatement = labeledStatement.getBody();
				
				addEdge(graph, labeledStatement, bodyStatement);
		
				//Ignore the labeled statement for now
				return from(graph, context.enterStatement(currentStatement), labeledStatement.getBody());
				
			} else if (currentStatement instanceof IfStatement) {
				IfStatement ifStatement = (IfStatement) currentStatement;
								
				Statement thenStatement = ifStatement.getThenStatement();
				Statement elseStatement = ifStatement.getElseStatement();

				//Compute the exit entries for an if statement depending on whether there is an else branch
				if (elseStatement == null) {					
					addEdge(graph, ifStatement, thenStatement);
					
					
					FlowResult thenResult = from(graph, context.enterStatement(ifStatement), thenStatement);
					
					FlowResult result = FlowResult.concat(
							ifStatement, 
							new Statement[] { ifStatement }, 
							new Statement[] { thenStatement }, 
							new FlowResult[] { thenResult },
							true);
					
					//If there is no else branch, then either the if statement or the last statement of the then-branch
					//can be used as previous statements
					return result;
				} else {					
					addEdge(graph, ifStatement, thenStatement);
					addEdge(graph, ifStatement, elseStatement);
					
					FlowResult thenResult = from(graph, context.enterStatement(ifStatement), thenStatement);
					FlowResult elseResult = from(graph, context.enterStatement(ifStatement), elseStatement);				
					
					
					FlowResult result = FlowResult.concat(
							ifStatement, 
							new Statement[0], 
							new Statement[] { thenStatement, elseStatement }, 
							new FlowResult[] { thenResult, elseResult },
							true);
					
					//If there is an else branch, then either the last statement of the then-branch
					//or the last statement of the else-branch can be used as previous statements
					return result;
				}		
				
			} else if (currentStatement instanceof SwitchStatement) {
				//If we look at a block, discard the block and only look at the statements in the block.
				SwitchStatement switchStatement = (SwitchStatement) currentStatement;
				Collection<Statement> previousStatements = Collections.emptyList();
				
				Context newContext = context.enterStatement(switchStatement);
				
				FlowResult result = FlowResult.create(); 
				
				//Iterate through all statements in the block and establish links between them.
				for (Object element : switchStatement.statements()) {
					Statement statement = (Statement) element;
					
					addEdges(graph, previousStatements, statement);
					
					FlowResult statementResult = from(graph, newContext, statement);
					//TODO: Is this correct?
					result.addTo(statement, new Statement[0], new Statement[] {statement}, new FlowResult[] {statementResult}, true);				
					
					previousStatements = statementResult.exits.get(statement); 					
				}
				
				result.exits.putAll(switchStatement, previousStatements);
				
				return result;
			
			} else if (currentStatement instanceof SwitchCase) {
				SwitchCase switchCase = (SwitchCase) currentStatement;
				
				Statement enclosingSwitch = context.enclosingSwitch();
				if (enclosingSwitch != null) { //Enclosing switch should never be null if the program is correctly typed.
					addEdge(graph, enclosingSwitch, switchCase);	
				}
				
				//Traverse further.
				return FlowResult.create(switchCase);
			
			} else if (currentStatement instanceof WhileStatement) {
				WhileStatement whileStatement = (WhileStatement) currentStatement;
				
				Statement bodyStatement = whileStatement.getBody();
				
				addEdge(graph, whileStatement, bodyStatement);
				
				//Create edges inside while-block with while as previous edge
				FlowResult bodyResult = from(graph, context.enterStatement(whileStatement), bodyStatement);
				
				//Add an edge from the end of the while block to the beginning of the while block
				addEdges(graph, bodyResult.exits.get(bodyStatement), whileStatement);
						
				FlowResult result = FlowResult.concat(
						whileStatement,
						new Statement[] { whileStatement },
						new Statement[] { bodyStatement },
						new FlowResult[] { bodyResult },
						false
						);
				
				//Move to the next statement in the main block
				return result;		
			
			} else if (currentStatement instanceof DoStatement) {
				DoStatement doStatement = (DoStatement) currentStatement;
				
				Statement bodyStatement = doStatement.getBody();
				
							
				//Create edges inside while-block with while as previous edge
				FlowResult bodyResult = from(graph, context.enterStatement(doStatement), bodyStatement);
				
				
				graph.addVertex(doStatement);
				//Add an edge from the end of the while block to the beginning of the while block
				for (Statement exit : bodyResult.exits.get(bodyStatement)) { //DO NOT USE ADDEDGES HERE, because we need an edge to the real do statement and not to
												 //the body.
					graph.addEdge(exit, doStatement);
				}
				
				//Link to bodystatement has already been added in addEdge
				//graph.addVertex(bodyStatement);
				//addEdge(graph, doStatement, bodyStatement);	
				addEdge(graph, doStatement, bodyStatement);
								
				//Move to the next statement in the main block
				return FlowResult.concat(
						doStatement,
						new Statement[] { doStatement },
						new Statement[] { bodyStatement },
						new FlowResult[] { bodyResult },
						false
						);
				
			} else if (currentStatement instanceof ForStatement) {
				ForStatement forStatement = (ForStatement) currentStatement;
				
				Statement bodyStatement = forStatement.getBody();
				
				addEdge(graph, forStatement, bodyStatement);
				
				//Create edges inside while-block with while as previous edge
				FlowResult bodyResult = from(graph, context.enterStatement(forStatement), bodyStatement);
				
				//Add an edge from the end of the while block to the beginning of the while block
				addEdges(graph, bodyResult.exits.get(bodyStatement), forStatement);
										
				//Move to the next statement in the main block
				return FlowResult.concat(
						forStatement,
						new Statement[] { forStatement },
						new Statement[] { bodyStatement },
						new FlowResult[] { bodyResult },
						false
						);					
			
			} else if (currentStatement instanceof ContinueStatement) {
				ContinueStatement continueStatement = (ContinueStatement) currentStatement;
												
				Statement enclosingLoop = context.enclosingContinueBlock(continueStatement.getLabel());
				if (enclosingLoop != null) { //Enclosing loop should never be null if the program is correctly typed.
					addEdge(graph, continueStatement, enclosingLoop);	
				}
				
				//Stop traversion of the block.
				return FlowResult.create();
			} else if (currentStatement instanceof BreakStatement) {
				BreakStatement breakStatement = (BreakStatement) currentStatement;
				
				FlowResult result = FlowResult.create();
				
				Statement enclosingBlock = context.enclosingBreakBlock(breakStatement.getLabel());
				if (enclosingBlock != null) {
					result.exits.put(enclosingBlock, breakStatement);
				}
				
				//Stop traversion of the block.
				return result;
			} else if (currentStatement instanceof ReturnStatement) {
				//Do not traverse further.
				return FlowResult.create();
			} else {
				//If the statement does not affect the control flow, then just go to the next statement.
				return FlowResult.create(currentStatement);
			}

		}

		private static Edge<Statement> addEdge(ControlFlowGraph graph, Statement sourceVertex, Statement targetVertex) {
			
			
			if (targetVertex instanceof DoStatement) {
				DoStatement doStatement = (DoStatement) targetVertex;
				
				graph.addVertex(sourceVertex);
				graph.addVertex(doStatement.getBody());
				
				return graph.addEdge(sourceVertex, doStatement.getBody());
			} else {
				
				graph.addVertex(sourceVertex);
				graph.addVertex(targetVertex);
				
				return graph.addEdge(sourceVertex, targetVertex);
			}			
		}
	
		
		private static void addEdges(ControlFlowGraph graph, Iterable<Statement> previousStatements, Statement currentStatement) {
			for (Statement previousStatement : previousStatements) {
				//Add an edge previousStatement --> currentStatement
				addEdge(graph, previousStatement, currentStatement);
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
