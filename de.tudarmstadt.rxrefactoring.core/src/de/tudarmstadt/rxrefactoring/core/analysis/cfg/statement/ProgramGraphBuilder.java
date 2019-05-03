package de.tudarmstadt.rxrefactoring.core.analysis.cfg.statement;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IEdge;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.expression.ExceptionIdentifier;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.expression.ExpressionGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.expression.ExpressionGraph.ExprAccess;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.Statements;

/**
 * This class contains the functionality to generate a control flow graph
 * from a statement.
 * 
 * @author mirko
 *
 */
class ProgramGraphBuilder {
	
	private final ProgramGraph graph;
	
	public ProgramGraphBuilder(ProgramGraph graph) {
		super();
		this.graph = graph;
	}
	
	public void process(Statement statement) {
		process(statement, Context.empty());
	}
	
	public ExprAccess addExpr(Expression expr) {
		return ExpressionGraph.addTo(expr, graph);
	}
	
	/**
	 * The context defines the nesting of statements.
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
		
		
		public boolean isEmpty() {
			return callBlocks.length == 0;
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
							
			for (Statement statement : callBlocks) {			
				if (label == null && Statements.isLoop(statement)
					|| label != null && statement instanceof LabeledStatement 
						&& Statements.isLoop(((LabeledStatement) statement).getBody()) 
						&& Objects.equals(label.getIdentifier(), ((LabeledStatement) statement).getLabel().getIdentifier())) {
					return statement;
				}
			}
			
			return null;				
		}
		
		public Statement enclosingBreakBlock(SimpleName label) {
			
			for (Statement statement : callBlocks) {	
				if (label == null) { //unlabeled break statement
					if (Statements.isLoop(statement) || statement instanceof SwitchStatement) {
						return statement;
					}					
				} else { //labeled break statement
					if (statement instanceof LabeledStatement && Objects.equals(label.getIdentifier(), ((LabeledStatement) statement).getLabel().getIdentifier())) {
						return statement;
					}
				}				
			}
			
			//TODO: When this happens there is an error in program, i.e. there is no statement matching the break. How to handle this?
			return null;
		}
		
		public SwitchStatement enclosingSwitch() {
			for (Statement element : callBlocks) {					
				if (element instanceof SwitchStatement) {
					return (SwitchStatement) element;
				}
			}
			
			return null;
		}
		
		public @Nullable Set<CatchClause> enclosingExceptionHandler(ExceptionIdentifier exception) {
			
			Set<CatchClause> clauses = Sets.newHashSet();
			
			for (Statement statement : callBlocks) {
				if (statement instanceof TryStatement) {
					TryStatement tryStatement = (TryStatement) statement;
					
					for (Object o : tryStatement.catchClauses()) {
						CatchClause clause = (CatchClause) o;						
						
						if (exception.isHandledBy(clause.getException().getType())) {
							clauses.add(clause);
							
							//If it is any exception it can be handeled by multiple catch clauses. Else it is only handled by a specific clause.
							if (!exception.isAnyException())
								return clauses;
						}
					}
				}
			}
			
			return clauses;
			
		}
		
		
		@Override
		public String toString() {
			return "Context" + Arrays.toString(callBlocks);
		}
	}
			
	
	/**
	 * Describes the exit nodes for a statement and possibly other statements
	 * affected by this statement. 
	 * 
	 * @author mirko
	 *
	 */
	private static class StatementExits {
		/**
		 * Multimap from statement to all exits that should be added to the CFG node of that statement.
		 */
		private final Multimap<Statement, ASTNode> exitMap;
		
		
		private StatementExits(Multimap<Statement, ASTNode> exits) {
			this.exitMap = exits;
		}
		
		private StatementExits() {
			this(newMultimap());
		}
		
		public Iterable<ASTNode> get(Statement node) {
			return exitMap.get(node);
		}
		
		public StatementExits add(Statement node, ASTNode... exits) {
			exitMap.putAll(node, Arrays.asList(exits));
			return this;
		}
		
		public StatementExits add(Statement node, Iterable<ASTNode> exits) {
			exitMap.putAll(node, exits);
			return this;
		}
		
		public StatementExits move(ASTNode from, Statement to) {
			exitMap.putAll(to, exitMap.removeAll(from));
			return this;
		}
		
		public StatementExits copyFrom(StatementExits exits) {
			exitMap.putAll(exits.exitMap);
			return this;
		}
		
		public StatementExits remove(ASTNode node) {
			exitMap.removeAll(node);
			return this;
		}
		
					
					
							
		private static Multimap<Statement, ASTNode> newMultimap() {
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
		public static StatementExits create() {		
			return new StatementExits(newMultimap());
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
	private StatementExits process(Statement currentStatement, Context context) {
					
		//Decide how to traverse further by looking at the current statement.
		//There should be a case for each statement that changes the control flow.			
		if (currentStatement instanceof Block) {				
			Block block = (Block) currentStatement;
							
			Iterable<ASTNode> previousNodes = Collections.singleton(block);
			
			Context newContext = context.enterStatement(block);
			
			StatementExits result = StatementExits.create(); 
			Statement statement = block;
			
			if (block.statements().isEmpty()) {
				graph.addVertex(block);
			} else for (Object element : block.statements()) {
				 //Iterate through all statements in the block and establish links between them.
				statement = (Statement) element;					
				addEdges(previousNodes, statement);
				
				StatementExits statementResult = process(statement, newContext);
				previousNodes = statementResult.get(statement);
				
				result.copyFrom(statementResult).remove(statement);					 					
			}
			
			//TODO Heuristic: If it is the outermost block then the last statement is an exit. Is this correct?
			if (context.isEmpty() && !graph.hasExitNodes()) {
				graph.addExitNode(statement);
			}
			
			return result.add(block, previousNodes);
			
		} else if (currentStatement instanceof LabeledStatement) {
			LabeledStatement labeledStatement = (LabeledStatement) currentStatement;
			
			Statement bodyStatement = labeledStatement.getBody();				
			addEdge(labeledStatement, bodyStatement);
	
			StatementExits bodyExits = process(labeledStatement.getBody(), context.enterStatement(currentStatement));
			
			//Ignore the labeled statement for now
			return StatementExits.create().copyFrom(bodyExits).move(bodyStatement, labeledStatement);
			
		} else if (currentStatement instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) currentStatement;
							
			Statement thenStatement = ifStatement.getThenStatement();
			Statement elseStatement = ifStatement.getElseStatement();
			
			ExprAccess conditionResult = addExpr(ifStatement.getExpression());
			addEdge(ifStatement, conditionResult.entry);
			addEdge(conditionResult.exit, thenStatement);
			handleExceptions(conditionResult, context);
			
			StatementExits thenResult = process(thenStatement, context.enterStatement(ifStatement));

			//Compute the exit entries for an if statement depending on whether there is an else branch
			if (elseStatement == null) {
				//If there is no else branch, then either the if statement or the last statement of the then-branch
				//can be used as previous statements
				return StatementExits.create().copyFrom(thenResult).move(thenStatement, ifStatement).add(ifStatement, conditionResult.exit);
			} else {
				
				addEdge(conditionResult.exit, elseStatement);					
				StatementExits elseResult = process(elseStatement, context.enterStatement(ifStatement));						
	
				//If there is an else branch, then either the last statement of the then-branch
				//or the last statement of the else-branch can be used as previous statements
				return StatementExits.create()
						.copyFrom(thenResult).copyFrom(elseResult)
						.move(thenStatement, ifStatement)
						.move(elseStatement, ifStatement);				
			}		
			
		} else if (currentStatement instanceof SwitchStatement) {
			SwitchStatement switchStatement = (SwitchStatement) currentStatement;
			
			ExprAccess expressionResult = addExpr(switchStatement.getExpression());
			addEdge(switchStatement, expressionResult.entry);	
			handleExceptions(expressionResult, context);
			
			Iterable<ASTNode> previousNodes = Collections.singleton(expressionResult.exit);
			
			Context newContext = context.enterStatement(switchStatement);
			
			StatementExits result = StatementExits.create(); 
			
			for (Object element : switchStatement.statements()) {
				Statement statement = (Statement) element;		
				addEdges(previousNodes, statement);
				
				if (statement instanceof SwitchCase) {
					addEdge(expressionResult.exit, statement);
				}
				
				StatementExits statementResult = process(statement, newContext);
				previousNodes = statementResult.get(statement);					
				
				//TODO: Add breaks?
				result.copyFrom(statementResult).remove(statement);					 					
			}
			
			return result.add(switchStatement, previousNodes);
		
		} else if (currentStatement instanceof SwitchCase) {
			SwitchCase switchCase = (SwitchCase) currentStatement;
			
			Expression expression = switchCase.getExpression();
			
			if (expression == null) {
				return StatementExits.create().add(currentStatement, currentStatement);
			}
			
			ExprAccess expressionResult = addExpr(expression);
			addEdge(switchCase, expressionResult.entry);		
			handleExceptions(expressionResult, context);
			
			//Traverse further.
			return StatementExits.create().add(switchCase, expressionResult.exit);
		
		} else if (currentStatement instanceof WhileStatement) {
			WhileStatement whileStatement = (WhileStatement) currentStatement;
			
			Statement bodyStatement = whileStatement.getBody();
			
			ExprAccess conditionResult = addExpr(whileStatement.getExpression());
			addEdge(whileStatement, conditionResult.entry);
			addEdge(conditionResult.exit, bodyStatement);
			handleExceptions(conditionResult, context);
						
			//Create edges inside while-block with while as previous edge
			StatementExits bodyResult = process(bodyStatement, context.enterStatement(whileStatement));
			
			//Add an edge from the end of the while block to the beginning of the while block
			addEdges(bodyResult.get(bodyStatement), conditionResult.entry);
					
					
			//Move to the next statement in the main block
			return StatementExits.create().copyFrom(bodyResult).remove(bodyStatement).add(whileStatement, conditionResult.exit);		
		
		} else if (currentStatement instanceof DoStatement) {
			DoStatement doStatement = (DoStatement) currentStatement;
			
			Statement bodyStatement = doStatement.getBody();
			
			addEdge(doStatement, bodyStatement);
			
			//Create edges inside while-block with while as previous edge
			StatementExits bodyResult = process(bodyStatement, context.enterStatement(doStatement));
			
			ExprAccess conditionResult = addExpr(doStatement.getExpression());
			addEdges(bodyResult.get(bodyStatement), conditionResult.entry);
			addEdge(conditionResult.exit, bodyStatement);	
			handleExceptions(conditionResult, context);
			
			
			//Add an edge from the end of the while block to the beginning of the while block
			addEdges(bodyResult.get(bodyStatement), conditionResult.entry);
					
			return StatementExits.create().copyFrom(bodyResult).remove(bodyStatement).add(doStatement, conditionResult.exit);	
		
		} else if (currentStatement instanceof ForStatement) {
			ForStatement forStatement = (ForStatement) currentStatement;
			
			Statement bodyStatement = forStatement.getBody();
			
			ASTNode previousNode = forStatement;							
			for (Object o : forStatement.initializers()) {
				Expression initializer = (Expression) o;
				
				ExprAccess initializerResult = addExpr(initializer);
				addEdge(previousNode, initializerResult.entry);
				previousNode = initializerResult.exit;		
				handleExceptions(initializerResult, context);
			}
		
			ExprAccess conditionResult = null;
			if (forStatement.getExpression() != null) {
				conditionResult = addExpr(forStatement.getExpression());
				addEdge(previousNode, conditionResult.entry);
				previousNode = conditionResult.exit;
				handleExceptions(conditionResult, context);
			}
			
			StatementExits bodyExits = process(bodyStatement, context.enterStatement(forStatement));
			addEdge(previousNode, bodyStatement);
			
			previousNode = null;
			for (Object o : forStatement.updaters()) {
				Expression updater = (Expression) o;
				
				ExprAccess updaterResult = addExpr(updater);
				if (previousNode == null) {
					addEdges(bodyExits.get(bodyStatement), updaterResult.entry);
				} else {
					addEdge(previousNode, updaterResult.entry);
				}					
				handleExceptions(updaterResult, context);
				previousNode = updaterResult.exit;					
			}
			
			ASTNode toNode = conditionResult != null ? conditionResult.entry : bodyStatement;
			
			if (previousNode == null) {
				addEdges(bodyExits.get(bodyStatement), toNode);
			} else {
				addEdge(previousNode, toNode);
			}
			
			
			if (conditionResult == null) {
				return StatementExits.create().copyFrom(bodyExits).remove(bodyStatement);
			} else {
				return StatementExits.create().copyFrom(bodyExits).remove(bodyStatement).add(forStatement, conditionResult.exit);
			}
		} else if (currentStatement instanceof EnhancedForStatement) {
			EnhancedForStatement enhancedForStatement = (EnhancedForStatement) currentStatement;
			
			Statement bodyStatement = enhancedForStatement.getBody();
			
			ExprAccess exprResult = addExpr(enhancedForStatement.getExpression());
			addEdge(enhancedForStatement, exprResult.entry);
			addEdge(exprResult.exit, bodyStatement);
			handleExceptions(exprResult, context);
						
			//Create edges inside while-block with while as previous edge
			StatementExits bodyResult = process(bodyStatement, context.enterStatement(enhancedForStatement));
			
			//Add an edge from the end of the while block to the beginning of the while block
			addEdges(bodyResult.get(bodyStatement), bodyStatement);
					
					
			//Move to the next statement in the main block
			return StatementExits.create().copyFrom(bodyResult)
					.add(enhancedForStatement, exprResult.exit)
					.move(bodyStatement, enhancedForStatement);		
			
		} else if (currentStatement instanceof TryStatement) {
			TryStatement tryStatement = (TryStatement) currentStatement;
			
			if (!tryStatement.resources().isEmpty())
				Log.error(getClass(), "try-with-resources statement is unsupported by CFG");
			
			//TODO add try-with-resources
			Block body = tryStatement.getBody();			
			addEdge(tryStatement, body);
			
			StatementExits bodyExits = process(body, context.enterStatement(tryStatement));						
			
			Block finallyBlock = tryStatement.getFinally();
			if (finallyBlock == null) {
				StatementExits result = StatementExits.create().copyFrom(bodyExits).move(body, tryStatement);
				
				tryStatement.catchClauses().forEach(o -> {
					CatchClause clause = (CatchClause) o;
					addEdge(clause, clause.getBody());
					
					StatementExits clauseExit =  process(clause.getBody(), context);
					result.copyFrom(clauseExit).move(clause.getBody(), tryStatement);
				});
				
				return result; 
			} else {
				
				//TODO Improve finally block. At the moment only the exits of the
				//try statement and the exits of the catch clauses enter the
				//finally block.
				
				Log.error(getClass(), "finally is only partially supported.");
				bodyExits.get(body).forEach(exit -> addEdge(exit, finallyBlock));
				
				tryStatement.catchClauses().forEach(o -> {
					CatchClause clause = (CatchClause) o;
					addEdge(clause, clause.getBody());
					
					StatementExits clauseExit =  process(clause.getBody(), context);
					clauseExit.get(clause.getBody()).forEach(exit -> addEdge(exit, finallyBlock));
				});
				
				StatementExits finallyExits = process(finallyBlock, context);
				
				return StatementExits.create().copyFrom(finallyExits).move(finallyBlock, tryStatement);
			}			
		} else if (currentStatement instanceof ThrowStatement) {
			ThrowStatement throwStatement = (ThrowStatement) currentStatement;
			
			ITypeBinding type = throwStatement.getExpression().resolveTypeBinding();
			
			ExceptionIdentifier id = type == null ? ExceptionIdentifier.ANY_EXCEPTION : ExceptionIdentifier.createFrom(type);
			
			Set<CatchClause> clauses = context.enclosingExceptionHandler(id);			
			if (clauses.isEmpty()) {
				graph.addExitNode(throwStatement);
			} else {
				clauses.forEach(clause -> addEdge(throwStatement, clause));
			}						
			
			return StatementExits.create();
						
		} else if (currentStatement instanceof ContinueStatement) {
			ContinueStatement continueStatement = (ContinueStatement) currentStatement;
											
			Statement enclosingLoop = context.enclosingContinueBlock(continueStatement.getLabel());
			if (enclosingLoop != null) { //Enclosing loop should never be null if the program is correctly typed.
				addEdge(continueStatement, enclosingLoop);	
			}
			
			//Stop traversion of the block.
			return StatementExits.create();
		} else if (currentStatement instanceof BreakStatement) {
			BreakStatement breakStatement = (BreakStatement) currentStatement;
			
			StatementExits result = StatementExits.create();
			
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
				ExprAccess expressionResult = addExpr(expression);
				addEdge(returnStatement, expressionResult.entry);
				handleExceptions(expressionResult, context);
			}
			
			//Add exit and do not traverse further.
			graph.addExitNode(returnStatement);
			return StatementExits.create();
		} else if (currentStatement instanceof ExpressionStatement) {				
			ExpressionStatement expressionStatement = (ExpressionStatement) currentStatement;
			Expression expression = expressionStatement.getExpression();
							
			ExprAccess expressionResult = addExpr(expression);
			addEdge(expressionStatement, expressionResult.entry);		
			handleExceptions(expressionResult, context);
			
			//Do not traverse further.
			return StatementExits.create().add(expressionStatement, expressionResult.exit);
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
					ExprAccess initializerResult = addExpr(initializer);
					addEdge(previousNode, initializerResult.entry);
					addEdge(initializerResult.exit, name);
					handleExceptions(initializerResult, context);
					previousNode = name;
				}
				
			}
							
			//Do not traverse further.
			return StatementExits.create().add(varStatement, previousNode);
		} else {
			//If the statement does not affect the control flow, then just go to the next statement.
			return StatementExits.create().add(currentStatement, currentStatement);
		}		
	}
	
	private void handleExceptions(ExprAccess exprAccess, Context context) {
		exprAccess.exceptions.entries().forEach(entry -> {
			ExceptionIdentifier id = entry.getKey();
			Expression expr = entry.getValue();
			
			Set<CatchClause> clauses = context.enclosingExceptionHandler(id);
			
			if (clauses.isEmpty()) {
				graph.addExitNode(expr);
			} else {
				clauses.forEach(clause -> addEdge(expr, clause));
			}			
		});		
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