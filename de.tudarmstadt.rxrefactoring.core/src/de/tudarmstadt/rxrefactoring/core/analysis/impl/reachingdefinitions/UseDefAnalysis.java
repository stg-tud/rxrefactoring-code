package de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.NotConvergingException;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.strategy.IDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use.Kind;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Expressions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public final class UseDefAnalysis extends DataFlowAnalysis<ASTNode, UseDef> {

	private final Strategy strategy;

	private UseDefAnalysis() {
		this(new Strategy());
	}

	private UseDefAnalysis(Strategy strategy) {
		super(strategy, DataFlowAnalysis.traversalForwards());
		this.strategy = strategy;
	}

	public static UseDefAnalysis create() {
		return new UseDefAnalysis();
	}

	private static class Strategy implements IDataFlowStrategy<ASTNode, UseDef> {

		private Map<ASTNode, ReachingDefinition> reachingDefinitions;

		public void setReachingDefinitions(Map<ASTNode, ReachingDefinition> reaching) {
			//this.reachingDefinitions.putAll(reaching);
			if (this.reachingDefinitions == null) {
				this.reachingDefinitions = reaching;
			} else {
				this.reachingDefinitions.putAll(reaching);
				for (Entry<ASTNode, ReachingDefinition> e :reaching.entrySet()) {
					Optional<LambdaExpression> l = ASTNodes.findParent(e.getKey(), LambdaExpression.class);
					if (l.isPresent()) {
						Optional<MethodInvocation> mi = ASTNodes.findParent(l.get(), MethodInvocation.class);
						if (mi.isPresent() && this.reachingDefinitions.containsKey(l.get()))
							this.reachingDefinitions.put(e.getKey(), ReachingDefinition.merge(Arrays.asList(this.reachingDefinitions.get(mi.get()), e.getValue())));
					}
				}
			}
		}

		@Override
		public UseDef entryResult() {
			return UseDef.empty().build();
		}

		@Override
		public UseDef initResult() {
			return UseDef.empty().build();
		}

		@Override
		public UseDef mergeAll(final Iterable<UseDef> useDefs) {
			UseDef.Builder builder = UseDef.empty();
			useDefs.forEach(builder::addAll);
			return builder.build();
		}

		@Nullable
		private Name tryCast(Expression expression) {
			return expression instanceof Name ? (Name) expression : null;
		}

		@Override
		public UseDef transform(final ASTNode astNode, final UseDef input) {
			Multimap<Expression, Use> usesByExpression = Multimaps.newMultimap(new HashMap<>(), HashSet::new);

			if (astNode instanceof MethodInvocation) {
				
				MethodInvocation invocation = (MethodInvocation) astNode;
				
				List<Object> args = invocation.arguments();
				
				Expression callee = invocation.getExpression();
				Name calleeName = tryCast(callee);
				
				// Lambda expression
				if (args.size()==1 && args.get(0) instanceof LambdaExpression) {
					LambdaExpression lambda = (LambdaExpression) args.get(0);
					List<Object> lambdaArgs = lambda.parameters();
					if  (args.size()==1 && lambdaArgs.get(0) instanceof VariableDeclarationFragment) {
						Name name = ((VariableDeclarationFragment) lambdaArgs.get(0)).getName();
						usesByExpression.put(callee, new Use(Kind.VARIABLE_DECL, name, invocation));
					}
				} else {
					// The method is invoked on the object
					usesByExpression.put(callee, new Use(Kind.METHOD_INVOCATION, calleeName, invocation));
					// The objects are passed as parameters
					for (Object parameterObj : invocation.arguments()) {
						Expression parameter = (Expression) parameterObj;
						Name name = tryCast(parameter);
						usesByExpression.put(parameter, new Use(Kind.METHOD_PARAMETER, name, invocation));
				}
				}
			} else if (astNode instanceof ReturnStatement) {
				ReturnStatement returnStatement = (ReturnStatement) astNode;

				Expression result = returnStatement.getExpression();
				Name name = tryCast(result);
				usesByExpression.put(result, new Use(Kind.RETURN, name, returnStatement));
			} else if (astNode instanceof Assignment) {
				Assignment assignment = (Assignment) astNode;
				Expression rightSide = assignment.getRightHandSide();
				Expression leftSide = assignment.getLeftHandSide();
				if (leftSide instanceof SimpleName) {
					// The new name is added to the name field
					SimpleName name = (SimpleName) leftSide;
					IBinding binding = name.resolveBinding();
					if (binding instanceof IVariableBinding) {
						if (((IVariableBinding)binding).isField())
							usesByExpression.put(rightSide, new Use(Kind.FIELD_ASSIGN, name, assignment));
						else
							usesByExpression.put(rightSide, new Use(Kind.ASSIGN, name, assignment));
					}
				}
			} else if (astNode instanceof VariableDeclarationStatement
					|| astNode instanceof VariableDeclarationExpression) {				
				
				List fragments = null;
				if (astNode instanceof VariableDeclarationStatement) {
					fragments = ((VariableDeclarationStatement) astNode).fragments();
				} else if (astNode instanceof VariableDeclarationExpression) {
					fragments = ((VariableDeclarationExpression) astNode).fragments();
				} else {
					fragments = Collections.emptyList();
				}

				for (Object o : fragments) {
					VariableDeclarationFragment fragment = (VariableDeclarationFragment) o;

					Expression expression = fragment.getInitializer();
					if (expression != null) {
						// The new name is added to the name field
						Name name = fragment.getName();
						usesByExpression.put(expression, new Use(Kind.VARIABLE_DECL, name, astNode));
					}
				}

				//return input;
			} else if (astNode instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration decl = (SingleVariableDeclaration) astNode;				
				
				Expression expression = decl.getInitializer();
				if (expression != null) {
					// The new name is added to the name field
					Name name = decl.getName();
					usesByExpression.put(expression, new Use(Kind.VARIABLE_DECL, name, astNode));
				}	
					
					
			}  else if (astNode instanceof EnhancedForStatement) {
				EnhancedForStatement forStatement = (EnhancedForStatement) astNode;
				
				//TODO: variable is not assigned to the expression. To what is the variable assigned instead?
				usesByExpression.put(forStatement.getExpression(), new Use(Kind.VARIABLE_DECL, forStatement.getParameter().getName(), astNode));
			} else {
				return input;
			}

			UseDef.Builder builder = input.builder();
			for (Map.Entry<Expression, Use> entry : usesByExpression.entries()) {
				Expression expression = entry.getKey();
				Name name = tryCast(expression);
				Collection<Expression> definitions;
				if (name == null) {
					definitions = Collections.singleton(expression);
				} else {
					definitions = lookupDefinitions(astNode, name);
				}
				definitions.forEach(definition -> builder.addUse(definition, entry.getValue()));
			}
			return builder.build();
		}

		private Collection<Expression> lookupDefinitions(final ASTNode astNode, final Name name) {
			return reachingDefinitions.get(astNode).get(Expressions.resolveVariableBinding(name));
		}
	}

	@Override
	public <Output> Output apply(final IControlFlowGraph<ASTNode> cfg,
			final IDataFlowExecutionFactory<ASTNode, UseDef, Output> executionFactory) throws NotConvergingException {
		Map<ASTNode, ReachingDefinition> reaching;
		try {
			ReachingDefinitionsAnalysis analysis = ReachingDefinitionsAnalysis.create();
			reaching = analysis.apply(cfg, analysis.mapExecutor());
		} catch (NotConvergingException e) {
			reaching = (Map<ASTNode, ReachingDefinition>) e.getUnfinishedOutput();
		}
		
		strategy.setReachingDefinitions(reaching);
	
		return super.apply(cfg, executionFactory);
	}
}
