package de.tudarmstadt.rxrefactoring.core.analysis.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.strategy.IDataFlowStrategy;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

public class ReachingDefinitionAnalysis extends DataFlowAnalysis<ASTNode, Multimap<String, Expression>> {

	public static ReachingDefinitionAnalysis create() {
		return new ReachingDefinitionAnalysis();
	}
	
	protected ReachingDefinitionAnalysis() {
		super(new ReachingDefinitionsStrategy(), traversalForwards());		
	}
	
	private static class ReachingDefinitionsStrategy implements IDataFlowStrategy<ASTNode, Multimap<String, Expression>>  {

		@Override
		public @NonNull Multimap<String, Expression> entryResult() {
			return Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
		}

		@Override
		public @NonNull Multimap<String, Expression> initResult() {
			return Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
		}

		@Override
		public @NonNull Multimap<String, Expression> mergeAll(
				@NonNull Collection<Multimap<String, Expression>> results) {
			
			Multimap<String, Expression> res = Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());			
			results.forEach(m -> res.putAll(m));			
			return res;
		}

		@Override
		public @NonNull Multimap<String, Expression> transform(@NonNull ASTNode vertex,
				@NonNull Multimap<String, Expression> input) {
			
			Multimap<String, Expression> res = Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
			res.putAll(input);
			
			if (vertex instanceof Assignment) {
				Assignment assignment = (Assignment) vertex;
				
				Expression lhs = assignment.getLeftHandSide();
				Expression rhs = assignment.getRightHandSide();
				
				if (lhs instanceof Name) {
					putExpression(res, (Name) lhs, rhs);					
				} else {
					Log.error(getClass(), "Assignment to non-name");
				}
			} else if (vertex instanceof VariableDeclarationStatement) {
				VariableDeclarationStatement varDecl = (VariableDeclarationStatement) vertex;
				
				for (Object o : varDecl.fragments()) {
					VariableDeclarationFragment fragment = (VariableDeclarationFragment) o;
					
					if (fragment.getInitializer() != null) {
						putExpression(res, fragment.getName(), fragment.getInitializer());						
					}
				}
			}
			
			return res;
		}
		
		private void putExpression(Multimap<String, Expression> map, Name key, Expression value) {
			String id = nameAsString(key);
			map.removeAll(id);
			
			Collection<Expression> val;
			if (value instanceof Name) {
				String valId = nameAsString((Name) value);
				if (map.containsKey(valId)) {
					val = map.get(valId); 
				} else {
					val = Sets.newHashSet(value);
				}
			} else {
				val = Sets.newHashSet(value);
			}
			
			map.putAll(id, val);
		}
		
		private String nameAsString(Name name) {
			IBinding binding = name.resolveBinding();	
						 
			if (binding == null)
				return name.getFullyQualifiedName();
			else
				return binding.getName();
		}

		
		
	}
	
}