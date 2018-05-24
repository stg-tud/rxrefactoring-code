package de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.strategy.IDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.utils.Expressions;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

public class ReachingDefinitionsAnalysis extends DataFlowAnalysis<ASTNode, ReachingDefinition> {

	public static ReachingDefinitionsAnalysis create() {
		return new ReachingDefinitionsAnalysis();
	}

	protected ReachingDefinitionsAnalysis() {
		super(new ReachingDefinitionsStrategy(), traversalForwards());
	}

	private static class ReachingDefinitionsStrategy implements IDataFlowStrategy<ASTNode, ReachingDefinition> {

		@Override
		public @NonNull ReachingDefinition entryResult() {
			return ReachingDefinition.empty();
		}

		@Override
		public @NonNull ReachingDefinition initResult() {
			return ReachingDefinition.empty();
		}

		@Override
		public @NonNull ReachingDefinition mergeAll(@NonNull Iterable<ReachingDefinition> results) {
			return ReachingDefinition.merge(results);
		}

		@Override
		public @NonNull ReachingDefinition transform(@NonNull ASTNode vertex, @NonNull ReachingDefinition input) {

			caseDistinction: {
				if (vertex instanceof Assignment) {
					Assignment assignment = (Assignment) vertex;

					Expression lhs = assignment.getLeftHandSide();
					Expression rhs = assignment.getRightHandSide();
					Assignment.Operator op = assignment.getOperator();

					IVariableBinding variable = Expressions.resolveVariableBinding(lhs);

					if (variable == null) {
						Log.error(getClass(), "assignment to non-variable: " + lhs);
						break caseDistinction;
					}

					// TODO: Do we need to differentiate here? WHat is the meaning?
					if (Assignment.Operator.ASSIGN.equals(op)) {
						return input.replace(variable, rhs);
					} else {
						return input.replace(variable, rhs);
					}

				} else if (vertex instanceof PostfixExpression || vertex instanceof PrefixExpression) {
					Expression expression;
					if (vertex instanceof PostfixExpression) {
						PostfixExpression postfixExpr = (PostfixExpression) vertex;
						expression = postfixExpr.getOperand();
					} else if (vertex instanceof PrefixExpression) {
						PrefixExpression prefixExpr = (PrefixExpression) vertex;

						if (PrefixExpression.Operator.DECREMENT.equals(prefixExpr.getOperator())
								|| PrefixExpression.Operator.INCREMENT.equals(prefixExpr.getOperator())) {
							expression = prefixExpr.getOperand();
						} else { // other operators do not assign anything
							break caseDistinction;
						}
					} else {
						Log.error(getClass(), "found neither postfix nor prefix expression");
						break caseDistinction;
					}
					
					IVariableBinding variable = Expressions.resolveVariableBinding(expression);

					if (variable == null) {
						Log.error(getClass(), "assignment to non-variable.");
						break caseDistinction;
					}
					
					return input.replace(variable, (Expression) vertex);					

				} else if (vertex instanceof VariableDeclarationStatement
						|| vertex instanceof VariableDeclarationExpression) {
					
					
					
					List fragments = null;
					if (vertex instanceof VariableDeclarationStatement) {
						fragments = ((VariableDeclarationStatement) vertex).fragments();
					} else if (vertex instanceof VariableDeclarationExpression) {
						fragments = ((VariableDeclarationExpression) vertex).fragments();
					} else {
						fragments = Collections.emptyList();
					}

					for (Object o : fragments) {
						VariableDeclarationFragment fragment = (VariableDeclarationFragment) o;

						if (fragment.getInitializer() != null) {
							IVariableBinding variable = fragment.resolveBinding();

							input = input.replace(variable, fragment.getInitializer());
						}
					}

					return input;
				} else if (vertex instanceof SingleVariableDeclaration) {
					SingleVariableDeclaration decl = (SingleVariableDeclaration) vertex;
					
					if (decl.getInitializer() != null) {
						IVariableBinding variable = decl.resolveBinding();
						input = input.replace(variable, decl.getInitializer());
					}
				} else if (vertex instanceof EnhancedForStatement) {
					EnhancedForStatement forStatement = (EnhancedForStatement) vertex;
					
					IVariableBinding variable = forStatement.getParameter().resolveBinding();

					//TODO: variable is not assigned to the expression. To what is the variable assigned instead?										
					input = input.replace(variable, forStatement.getExpression());
				} else if (vertex instanceof MethodInvocation) {
					MethodInvocation methodInvocation = (MethodInvocation) vertex;
					Expression callee = methodInvocation.getExpression();
					List<Object> args = methodInvocation.arguments();
					if (args.size()==1 && args.get(0) instanceof LambdaExpression) {
						LambdaExpression lambda = (LambdaExpression) args.get(0);
						List<Object> lambdaParams = lambda.parameters();
						if  (lambdaParams.size()==1 && lambdaParams.get(0) instanceof VariableDeclarationFragment) {
							IVariableBinding variable = ((VariableDeclarationFragment) lambdaParams.get(0)).resolveBinding();									
							input = input.replace(variable, callee);
						}
					}
				}
			}

			return input;
		}

	}

}