package de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.strategy.IDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use.Kind;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ReturnStatement;

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
			this.reachingDefinitions = reaching;
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

				Expression callee = invocation.getExpression();
				Name calleeName = tryCast(callee);
				// The method is invoked on the object
				usesByExpression.put(callee, new Use(Kind.METHOD_INVOCATION, calleeName, invocation));

				// The objects are passed as parameters
				for (Object parameterObj : invocation.arguments()) {
					Expression parameter = (Expression) parameterObj;
					Name name = tryCast(parameter);
					usesByExpression.put(parameter, new Use(Kind.METHOD_PARAMETER, name, invocation));
				}
			} else if (astNode instanceof ReturnStatement) {
				ReturnStatement returnStatement = (ReturnStatement) astNode;

				Expression result = returnStatement.getExpression();
				Name name = tryCast(result);
				usesByExpression.put(result, new Use(Kind.RETURN, name, returnStatement));
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
			return reachingDefinitions.get(astNode).get(name);
		}
	}

	@Override
	public <Output> Output apply(final IControlFlowGraph<ASTNode> cfg,
			final IDataFlowExecutionFactory<ASTNode, UseDef, Output> executionFactory) {
		ReachingDefinitionsAnalysis analysis = ReachingDefinitionsAnalysis.create();
		Map<ASTNode, ReachingDefinition> reaching = analysis.apply(cfg, analysis.mapExecutor());
		strategy.setReachingDefinitions(reaching);
		return super.apply(cfg, executionFactory);
	}
}
