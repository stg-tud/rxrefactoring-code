package de.tudarmstadt.rxrefactoring.core.analysis;


import com.google.common.collect.Sets;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.strategy.SetDataFlowStrategy;
import java.util.Set;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class Analyses {

	public static final DataFlowAnalysis<Statement, Set<String>> VARIABLE_NAME_ANALYSIS =
			DataFlowAnalysis.create(new SetDataFlowStrategy<Statement, String>() {

				private Set<String> findVariableNamesIn(Statement statement) {
					final Set<String> result = Sets.newHashSet();

					if (statement instanceof VariableDeclarationStatement) {
						VariableDeclarationStatement variableDeclaration = (VariableDeclarationStatement) statement;
						for (Object element : variableDeclaration.fragments()) {
							result.add(((VariableDeclarationFragment) element).getName().getIdentifier());
						}
					}

					return result;
				}

				@Override
				public Set<String> transform(Statement statement, Set<String> input) {
					Set<String> result = findVariableNamesIn(statement);
					result.addAll(input);
					return result;
				}
			}, DataFlowAnalysis.traversalForwards());
}
