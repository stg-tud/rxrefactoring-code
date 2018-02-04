package de.tudarmstadt.rxrefactoring.core.analysis.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.strategy.IDataFlowStrategy;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * Returns a Map of 'defs' and 'uses' instead of only def-multimaps.
 *
 * @author Kumar
 * @author Björn
 */
public final class UseDefAnalysis {

	public static class UseDefResult {

		public final Multimap<String, ASTNode> defs;
		public final Multimap<String, SimpleName> uses;

		private UseDefResult() {
			this.defs = Multimaps.newSetMultimap(Maps.newLinkedHashMap(), Sets::newLinkedHashSet);
			this.uses = Multimaps.newSetMultimap(Maps.newLinkedHashMap(), Sets::newLinkedHashSet);
		}

		private void putAll(UseDefResult other) {
			defs.putAll(other.defs);
			uses.putAll(other.uses);
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			final UseDefResult that = (UseDefResult) o;
			return Objects.equals(defs, that.defs) &&
					Objects.equals(uses, that.uses);
		}

		@Override
		public int hashCode() {
			return Objects.hash(defs, uses);
		}

		@Override
		public String toString() {
			return "UseDefResult{" +
					"defs=" + defs +
					", uses=" + uses +
					'}';
		}
	}

	// TODO just taken from the old code, terribly inaccurate
	public static final Predicate<ITypeBinding> IS_FUTURE = binding -> binding.getName().contains("Future");
	public static final Predicate<ITypeBinding> IS_ASYNC_TASK =
			binding -> binding.getQualifiedName().equals("android.os.AsyncTask");

	public static DataFlowAnalysis<ASTNode, UseDefResult> create(Predicate<ITypeBinding> typeCheck) {
		return DataFlowAnalysis.create(new DataFlowStrategy(typeCheck), DataFlowAnalysis.traversalForwards());
	}

	private UseDefAnalysis() {
	}

	private static <V> V getLast(Collection<V> collection) {
		Iterator<V> iterator = collection.iterator();
		V result = null;
		while (iterator.hasNext()) {
			result = iterator.next();
		}
		if (result == null) {
			throw new IllegalArgumentException("Collection was empty");
		}
		return result;
	}

	private static class DataFlowStrategy implements IDataFlowStrategy<ASTNode, UseDefResult> {

		private final Predicate<ITypeBinding> typeCheck;

		private DataFlowStrategy(final Predicate<ITypeBinding> typeCheck) {
			this.typeCheck = typeCheck;
		}

		//Changed to LinkedHashMap and LinkedHashSet for tracking traversal of graph
		@Override
		public UseDefResult entryResult() {
			return new UseDefResult();
		}

		@Override
		public UseDefResult initResult() {
			return new UseDefResult();
		}

		@Override
		public UseDefResult mergeAll(Iterable<UseDefResult> results) {

			UseDefResult result = new UseDefResult();

			for (UseDefResult tempMap : results) {
				result.defs.putAll(tempMap.defs);
				result.uses.putAll(tempMap.uses);
			}

			return result;
		}

		@Override
		public UseDefResult transform(ASTNode vertex, UseDefResult input) {
			UseDefResult result = new UseDefResult();
			result.putAll(input);

			//Extract variable definitions and uses
			if (vertex instanceof Assignment) {
				Expression leftExpr = ((Assignment) vertex).getLeftHandSide();
				Expression rightExpr = ((Assignment) vertex).getRightHandSide();

				if (typeCheck.test(leftExpr.resolveTypeBinding()) && leftExpr instanceof SimpleName) {
					String leftVarName = ((SimpleName) leftExpr).getIdentifier();
					if (rightExpr instanceof SimpleName) {
						String rightVarName = ((SimpleName) rightExpr).getIdentifier();

						//If definitions already present take the latest definition
						if (input.defs.containsKey(rightVarName)) {
							Collection<ASTNode> al = input.defs.get(rightVarName);
							ASTNode defValNode = getLast(al);
							result.defs.put(leftVarName, defValNode);
							return result;
						}
					}

					result.defs.put(leftVarName, rightExpr);
				}
			} else if (vertex instanceof VariableDeclarationStatement) {
				VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment)
						((VariableDeclarationStatement) vertex).fragments().get(0);

				//Check future definitions only
				if (typeCheck.test(varDeclFrag.resolveBinding().getType())) {
					if (varDeclFrag.getInitializer() != null) {
						result.defs.put(varDeclFrag.getName().toString(), varDeclFrag.getInitializer());
					} else {
						result.defs.put(varDeclFrag.getName().toString(), null);
					}
				}
			} else if (vertex instanceof MethodInvocation) {
				MethodInvocation invocation = (MethodInvocation) vertex;
				if (invocation.getExpression() != null) {
					Expression invoker = invocation.getExpression();
					String invokerObject = invoker.toString();
					if (input.defs.containsKey(invokerObject)) {
						Collection<ASTNode> al = input.defs.get(invokerObject);
						String useVal = getLast(al).toString();
						result.uses.put(useVal, invocation.getName());
					} else {
						result.uses.put(invokerObject, invocation.getName());
					}
				}
			}

			return result;
		}
	}
}
