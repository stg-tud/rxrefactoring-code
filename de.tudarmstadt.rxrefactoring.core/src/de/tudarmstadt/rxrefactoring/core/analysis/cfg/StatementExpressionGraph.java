package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.jgrapht.graph.AbstractBaseGraph;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.ExpressionGraphUtils.ExpressionGraphBuilder;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.ExpressionGraphUtils.Result;
import de.tudarmstadt.rxrefactoring.core.utils.Statements;

//TODO: Add Exceptions to the Control Flow Graph
public class StatementExpressionGraph extends AbstractBaseGraph<ASTNode, IEdge<ASTNode>>
		implements IControlFlowGraph<ASTNode> {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 4664573166055105543L;
	

	protected StatementExpressionGraph() {
		super((v1, v2) -> new SimpleEdge<>(v1, v2), false, true);		
	}	
	
	/**
	 * Creates a CFG from a statement block.
	 * 
	 * @param block
	 * @return
	 */
	public static StatementExpressionGraph from(Statement statement) {
		StatementExpressionGraph graph = new StatementExpressionGraph();
		StatementGraphBuilder builder = new StatementGraphBuilder(graph);
		builder.process(statement);
		return graph;
	}		
	
	/**
	 * This class contains the functionality to generate a control flow graph
	 * from a statement.
	 * 
	 * @author mirko
	 *
	 */
	private static class StatementGraphBuilder {
		
		private final IControlFlowGraph<? super ASTNode> graph;
		private final ExpressionGraphBuilder builder;
		
		public StatementGraphBuilder(IControlFlowGraph<? super ASTNode> graph) {
			super();
			this.graph = graph;
			builder = new ExpressionGraphBuilder(graph);
		}
		
		public void process(Statement statement) {
			process(statement, Context.empty());
		}
		
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
				
		
		private static class Exits {
			/**
			 * Multimap from statement to all exits that should be added to the CFG node of that statement.
			 */
			private final Multimap<ASTNode, ASTNode> exitMap;
			
			
			private Exits(Multimap<ASTNode, ASTNode> exits) {
				this.exitMap = exits;
			}
			
			private Exits() {
				this(newMultimap());
			}
			
			public Iterable<ASTNode> get(ASTNode node) {
				return exitMap.get(node);
			}
			
			public Exits add(ASTNode node, ASTNode... exits) {
				exitMap.putAll(node, Arrays.asList(exits));
				return this;
			}
			
			public Exits add(ASTNode node, Iterable<ASTNode> exits) {
				exitMap.putAll(node, exits);
				return this;
			}
			
			public Exits move(ASTNode from, ASTNode to) {
				exitMap.putAll(to, exitMap.removeAll(from));
				return this;
			}
			
			public Exits copyFrom(Exits exits) {
				exitMap.putAll(exits.exitMap);
				return this;
			}
			
			public Exits remove(ASTNode node) {
				exitMap.removeAll(node);
				return this;
			}
			
						
						
								
			private static Multimap<ASTNode, ASTNode> newMultimap() {
				return Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
			}
			
			/**
			 * Creates a FlowResult of a statement that has the specified statements
			 * as exit statements.
			 * 
			 * @param nodes The statements used as exit statements
			 * @return A non-null flow result that contains the specifed
			 * statements as exit statements.
			 */
			public static Exits create() {		
				return new Exits(newMultimap());
			}
			
						
			
			
			@Override
			public String toString() {
				return "FlowResult(" + exitMap.toString() + ")";
			}
		}
		
		
		/**
		 * Builds a control flow graph from a given statement.
		 * @param currentStatement
		 *            The statement that is currently looked on.
		 * @param staments
		 *            The current list of statements to work on, i.e. the statements in
		 *            a block.
		 * @param currentIndex
		 *            The index of the currently looked statement.
		 * @param previousStatements
		 *            The previous statements.
		 * 
		 * @return The last statement(s) that has been processed in a basic block.
		 */
		private Exits process(Statement currentStatement, Context context) {
						
			//Decide how to traverse further by looking at the current statement.
			//There should be a case for each statement that changes the control flow.			
			if (currentStatement instanceof Block) {				
				Block block = (Block) currentStatement;
								
				Iterable<ASTNode> previousNodes = Collections.singleton(block);
				
				Context newContext = context.enterStatement(block);
				
				Exits result = Exits.create(); 
				
				//Iterate through all statements in the block and establish links between them.
				for (Object element : block.statements()) {
					Statement statement = (Statement) element;					
					addEdges(previousNodes, statement);
					
					Exits statementResult = process(statement, newContext);
					previousNodes = statementResult.get(statement);
					
					result.copyFrom(statementResult).remove(statement);					 					
				}
				
				return result.add(block, previousNodes);
				
			} else if (currentStatement instanceof LabeledStatement) {
				LabeledStatement labeledStatement = (LabeledStatement) currentStatement;
				
				Statement bodyStatement = labeledStatement.getBody();				
				addEdge(labeledStatement, bodyStatement);
		
				Exits bodyExits = process(labeledStatement.getBody(), context.enterStatement(currentStatement));
				
				//Ignore the labeled statement for now
				return Exits.create().copyFrom(bodyExits).move(bodyStatement, labeledStatement);
				
			} else if (currentStatement instanceof IfStatement) {
				IfStatement ifStatement = (IfStatement) currentStatement;
								
				Statement thenStatement = ifStatement.getThenStatement();
				Statement elseStatement = ifStatement.getElseStatement();
				
				Result conditionResult = builder.from(ifStatement.getExpression());
				addEdge(ifStatement, conditionResult.entry);
				addEdge(conditionResult.exit, thenStatement);
				
				Exits thenResult = process(thenStatement, context.enterStatement(ifStatement));

				//Compute the exit entries for an if statement depending on whether there is an else branch
				if (elseStatement == null) {
					//If there is no else branch, then either the if statement or the last statement of the then-branch
					//can be used as previous statements
					return Exits.create().copyFrom(thenResult).move(thenStatement, ifStatement).add(ifStatement, conditionResult.exit);
				} else {
					
					addEdge(conditionResult.exit, elseStatement);					
					Exits elseResult = process(elseStatement, context.enterStatement(ifStatement));						
		
					//If there is an else branch, then either the last statement of the then-branch
					//or the last statement of the else-branch can be used as previous statements
					return Exits.create()
							.copyFrom(thenResult).copyFrom(elseResult)
							.move(thenStatement, ifStatement).move(elseStatement, ifStatement);					
				
				}		
				
			} else if (currentStatement instanceof SwitchStatement) {
				SwitchStatement switchStatement = (SwitchStatement) currentStatement;
				Result expressionResult = builder.from(switchStatement.getExpression());
				addEdge(switchStatement, expressionResult.entry);				
				
				Iterable<ASTNode> previousNodes = Collections.singleton(expressionResult.exit);
				
				Context newContext = context.enterStatement(switchStatement);
				
				Exits result = Exits.create(); 
				
				for (Object element : switchStatement.statements()) {
					Statement statement = (Statement) element;		
					addEdges(previousNodes, statement);
					
					if (statement instanceof SwitchCase) {
						addEdge(expressionResult.exit, statement);
					}
					
					Exits statementResult = process(statement, newContext);
					previousNodes = statementResult.get(statement);					
					
					//TODO: Add breaks?
					result.copyFrom(statementResult).remove(statement);					 					
				}
				
				return result.add(switchStatement, previousNodes);
			
			} else if (currentStatement instanceof SwitchCase) {
				SwitchCase switchCase = (SwitchCase) currentStatement;
				
				Expression expression = switchCase.getExpression();
				
				if (expression == null) {
					return Exits.create().add(currentStatement, currentStatement);
				}
				
				Result expressionResult = builder.from(expression);
				addEdge(switchCase, expressionResult.entry);				
				
				//Traverse further.
				return Exits.create().add(switchCase, expressionResult.exit);
			
			} else if (currentStatement instanceof WhileStatement) {
				WhileStatement whileStatement = (WhileStatement) currentStatement;
				
				Statement bodyStatement = whileStatement.getBody();
				
				Result conditionResult = builder.from(whileStatement.getExpression());
				addEdge(whileStatement, conditionResult.entry);
				addEdge(conditionResult.exit, bodyStatement);
							
				//Create edges inside while-block with while as previous edge
				Exits bodyResult = process(bodyStatement, context.enterStatement(whileStatement));
				
				//Add an edge from the end of the while block to the beginning of the while block
				addEdges(bodyResult.get(bodyStatement), conditionResult.entry);
						
						
				//Move to the next statement in the main block
				return Exits.create().copyFrom(bodyResult).remove(bodyStatement).add(whileStatement, conditionResult.exit);		
			
			} else if (currentStatement instanceof DoStatement) {
				DoStatement doStatement = (DoStatement) currentStatement;
				
				Statement bodyStatement = doStatement.getBody();
				
				addEdge(doStatement, bodyStatement);
				
				//Create edges inside while-block with while as previous edge
				Exits bodyResult = process(bodyStatement, context.enterStatement(doStatement));
				
				Result conditionResult = builder.from(doStatement.getExpression());
				addEdges(bodyResult.get(bodyStatement), conditionResult.entry);
				addEdge(conditionResult.exit, bodyStatement);				
				
				
				//Add an edge from the end of the while block to the beginning of the while block
				addEdges(bodyResult.get(bodyStatement), conditionResult.entry);
						
				return Exits.create().copyFrom(bodyResult).remove(bodyStatement).add(doStatement, conditionResult.exit);	
			
			} else if (currentStatement instanceof ForStatement) {
				ForStatement forStatement = (ForStatement) currentStatement;
				
				Statement bodyStatement = forStatement.getBody();
				
				ASTNode previousNode = forStatement;							
				for (Object o : forStatement.initializers()) {
					Expression initializer = (Expression) o;
					
					Result initializerResult = builder.from(initializer);
					addEdge(previousNode, initializerResult.entry);
					previousNode = initializerResult.exit;					
				}
			
				Result conditionResult = null;
				if (forStatement.getExpression() != null) {
					conditionResult = builder.from(forStatement.getExpression());
					addEdge(previousNode, conditionResult.entry);
					previousNode = conditionResult.exit;
				}
				
				Exits bodyExits = process(bodyStatement, context.enterStatement(forStatement));
				addEdge(previousNode, bodyStatement);
				
				previousNode = null;
				for (Object o : forStatement.updaters()) {
					Expression updater = (Expression) o;
					
					Result updaterResult = builder.from(updater);
					if (previousNode == null) {
						addEdges(bodyExits.get(bodyStatement), updaterResult.entry);
					} else {
						addEdge(previousNode, updaterResult.entry);
					}					
					previousNode = updaterResult.exit;					
				}
				
				ASTNode toNode = conditionResult != null ? conditionResult.entry : bodyStatement;
				
				if (previousNode == null) {
					addEdges(bodyExits.get(bodyStatement), toNode);
				} else {
					addEdge(previousNode, toNode);
				}
				
				
				if (conditionResult == null) {
					return Exits.create().copyFrom(bodyExits).remove(bodyStatement);
				} else {
					return Exits.create().copyFrom(bodyExits).remove(bodyStatement).add(forStatement, conditionResult.exit);
				}
								
			
			} else if (currentStatement instanceof ContinueStatement) {
				ContinueStatement continueStatement = (ContinueStatement) currentStatement;
												
				Statement enclosingLoop = context.enclosingContinueBlock(continueStatement.getLabel());
				if (enclosingLoop != null) { //Enclosing loop should never be null if the program is correctly typed.
					addEdge(continueStatement, enclosingLoop);	
				}
				
				//Stop traversion of the block.
				return Exits.create();
			} else if (currentStatement instanceof BreakStatement) {
				BreakStatement breakStatement = (BreakStatement) currentStatement;
				
				Exits result = Exits.create();
				
				Statement enclosingBlock = context.enclosingBreakBlock(breakStatement.getLabel());
				if (enclosingBlock != null) {
					result.add(enclosingBlock, breakStatement);
				}
				
				//Stop traversion of the block.
				return result;
			} else if (currentStatement instanceof ReturnStatement) {				
				ReturnStatement returnStatement = (ReturnStatement) currentStatement;
				Expression expression = returnStatement.getExpression();
				
				if (expression != null) {
					Result expressionResult = builder.from(expression);
					addEdge(returnStatement, expressionResult.entry);
				}
				
				//Do not traverse further.
				return Exits.create();
			} else if (currentStatement instanceof ExpressionStatement) {				
				ExpressionStatement expressionStatement = (ExpressionStatement) currentStatement;
				Expression expression = expressionStatement.getExpression();
								
				Result expressionResult = builder.from(expression);
				addEdge(expressionStatement, expressionResult.entry);				
				
				//Do not traverse further.
				return Exits.create().add(expressionStatement, expressionResult.exit);
			} else if (currentStatement instanceof VariableDeclarationStatement) {					
				VariableDeclarationStatement varStatement = (VariableDeclarationStatement) currentStatement;
				
				ASTNode previousNode = varStatement;
				
				for(Object o : varStatement.fragments()) {
					VariableDeclarationFragment fragment = (VariableDeclarationFragment) o;
					
					Expression name = fragment.getName();
					Expression initializer = fragment.getInitializer(); 
					if (initializer == null) {
						addEdge(previousNode, name);
						previousNode = name;
					} else {
						Result initializerResult = builder.from(initializer);
						addEdge(previousNode, initializerResult.entry);
						addEdge(initializerResult.exit, name);
						previousNode = name;
					}
					
				}
								
				//Do not traverse further.
				return Exits.create().add(varStatement, previousNode);
			} else {
				//If the statement does not affect the control flow, then just go to the next statement.
				return Exits.create().add(currentStatement, currentStatement);
			}
		}

		private IEdge<? super ASTNode> addEdge(ASTNode sourceVertex, ASTNode targetVertex) {
			graph.addVertex(sourceVertex);
			graph.addVertex(targetVertex);
			
			return graph.addEdge(sourceVertex, targetVertex);
		}
	
		
		private void addEdges(Iterable<ASTNode> previousNodes, ASTNode currentNode) {
			for (ASTNode previousNode : previousNodes) {
				//Add an edge previousStatement --> currentStatement
				addEdge(previousNode, currentNode);
			}			
		}			
	}
	

	
		
	
//	public static class Result {
//		final ASTNode[] entry;
//		final ASTNode[] exit;
//					
//		public Result(ASTNode[] entry, ASTNode[] exit) {
//			super();
//			this.entry = entry;
//			this.exit = exit;
//		}	
//		
//		public Result(ASTNode entry, ASTNode exit) {
//			super();
//			this.entry = new ASTNode[] { entry };
//			this.exit = new ASTNode[] { exit };
//		}
//		
//		@Override
//		public String toString() {
//			return "Result(entry=" + entry + ", exit=" + exit + ")";
//		}
//	}
	
	
//	private static class StatementGraphBuilder {
//		private final IControlFlowGraph<? super Statement> graph;
	//
//	public StatementGraphBuilder(IControlFlowGraph<? super Statement> graph) {
//		super();
//		this.graph = graph;
//	}
//		
//		public Result from(Statement currentStatement, Context context, ExpressionGraphBuilder builder) {
//			//Decide how to traverse further by looking at the current statement.
//			//There should be a case for each statement that changes the control flow.			
//			if (currentStatement instanceof Block) {
//				//If we look at a block, discard the block and only look at the statements in the block.
//				Block block = (Block) currentStatement;
//				Collection<Statement> previousStatements = Collections.singleton(block);
//				
//				Context newContext = context.enterStatement(block);
//				
//				ExitFlows result = ExitFlows.create(); 
//				
//				//Iterate through all statements in the block and establish links between them.
//				for (Object element : block.statements()) {
//					Statement statement = (Statement) element;
//					
//					addEdges(previousStatements, statement);
//					
//					ExitFlows statementResult = from(graph, newContext, statement);
//					result.addTo(statement, new Statement[0], new Statement[] {statement}, new ExitFlows[] {statementResult}, true);				
//					
//					previousStatements = statementResult.exits.get(statement); 					
//				}
//				
//				result.exits.putAll(block, previousStatements);
//				
//				return new Result(block, );
//				
//			} else if (currentStatement instanceof LabeledStatement) {
//				LabeledStatement labeledStatement = (LabeledStatement) currentStatement;
//				
//				Statement bodyStatement = labeledStatement.getBody();
//				
//				addEdge(graph, labeledStatement, bodyStatement);
//		
//				//Ignore the labeled statement for now
//				return from(graph, context.enterStatement(currentStatement), labeledStatement.getBody());
//				
//			} else if (currentStatement instanceof IfStatement) {
//				IfStatement ifStatement = (IfStatement) currentStatement;
//								
//				Statement thenStatement = ifStatement.getThenStatement();
//				Statement elseStatement = ifStatement.getElseStatement();
//
//				//Compute the exit entries for an if statement depending on whether there is an else branch
//				if (elseStatement == null) {					
//					addEdge(graph, ifStatement, thenStatement);
//					
//					
//					ExitFlows thenResult = from(graph, context.enterStatement(ifStatement), thenStatement);
//					
//					ExitFlows result = ExitFlows.concat(
//							ifStatement, 
//							new Statement[] { ifStatement }, 
//							new Statement[] { thenStatement }, 
//							new ExitFlows[] { thenResult },
//							true);
//					
//					//If there is no else branch, then either the if statement or the last statement of the then-branch
//					//can be used as previous statements
//					return result;
//				} else {					
//					addEdge(graph, ifStatement, thenStatement);
//					addEdge(graph, ifStatement, elseStatement);
//					
//					ExitFlows thenResult = from(graph, context.enterStatement(ifStatement), thenStatement);
//					ExitFlows elseResult = from(graph, context.enterStatement(ifStatement), elseStatement);				
//					
//					
//					ExitFlows result = ExitFlows.concat(
//							ifStatement, 
//							new Statement[0], 
//							new Statement[] { thenStatement, elseStatement }, 
//							new ExitFlows[] { thenResult, elseResult },
//							true);
//					
//					//If there is an else branch, then either the last statement of the then-branch
//					//or the last statement of the else-branch can be used as previous statements
//					return result;
//				}		
//				
//			}
//		}
//		
//		private IEdge<? super Statement> addEdge(Statement sourceVertex, Statement targetVertex) {
//			
//			
////			if (targetVertex instanceof DoStatement) {
////				DoStatement doStatement = (DoStatement) targetVertex;
////				
////				graph.addVertex(sourceVertex);
////				graph.addVertex(doStatement.getBody());
////				
////				return graph.addEdge(sourceVertex, doStatement.getBody());
////			} else {
//				
//				graph.addVertex(sourceVertex);
//				graph.addVertex(targetVertex);
//				
//				return graph.addEdge(sourceVertex, targetVertex);
////			}			
//		}
//	
//		
//		private void addEdges(Iterable<Statement> previousStatements, Statement currentStatement) {
//			for (Statement previousStatement : previousStatements) {
//				//Add an edge previousStatement --> currentStatement
//				addEdge(previousStatement, currentStatement);
//			}			
//		}
//	}
	
	
	
	public String listEdges() {
		String result = "";
		for (IEdge<ASTNode> edge : edgeSet()) {
			result += edge.toString() + ";\n";
		}
		return result;
	}

}
