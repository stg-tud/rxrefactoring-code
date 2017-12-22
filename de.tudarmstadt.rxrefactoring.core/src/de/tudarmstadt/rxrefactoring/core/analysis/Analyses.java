package de.tudarmstadt.rxrefactoring.core.analysis;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.analysis.flow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.strategy.IDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.analysis.strategy.SetDataFlowStrategy;

public class Analyses {
	
	public static final DataFlowAnalysis<ASTNode, Multimap<Expression, ASTNode>> USE_DEF_ANALYSIS =
			DataFlowAnalysis.create(new IDataFlowStrategy<ASTNode, Multimap<Expression, ASTNode>>() {

				@Override
				public Multimap<Expression, ASTNode> entryResult() {
					return Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
				}

				@Override
				public Multimap<Expression, ASTNode> initResult() {				
					return Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
				}

				@Override
				public Multimap<Expression, ASTNode> mergeAll(List<Multimap<Expression, ASTNode>> results) {
					
					Multimap<Expression, ASTNode> result = Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
					// TODO Auto-generated method stub
					
					
					return result;
				}

				@Override
				public Multimap<Expression, ASTNode> transform(ASTNode vertex, Multimap<Expression, ASTNode> input) {
					Multimap<Expression, ASTNode> result = Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
					
					// TODO Auto-generated method stub
					return result;
				}
				
			}, DataFlowAnalysis.TRAVERSAL_FORWARDS);
	
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
			}, DataFlowAnalysis.TRAVERSAL_FORWARDS);
}
