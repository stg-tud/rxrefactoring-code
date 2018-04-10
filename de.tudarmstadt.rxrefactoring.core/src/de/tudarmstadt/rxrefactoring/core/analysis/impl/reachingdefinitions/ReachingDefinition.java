package de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions;

import java.util.Collection;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IVariableBinding;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.utils.Expressions;

public class ReachingDefinition {
	private ImmutableSetMultimap<IVariableBinding, Expression> definitions;

	ReachingDefinition(ImmutableSetMultimap<IVariableBinding, Expression> defintions) {
		this.definitions = defintions;
	}

	static ReachingDefinition empty() {
		ReachingDefinition result = new ReachingDefinition(ImmutableSetMultimap.of());
		return result;
	}

	static ReachingDefinition merge(Iterable<ReachingDefinition> iterable) {
		ImmutableSetMultimap.Builder<IVariableBinding, Expression> builder = ImmutableSetMultimap.builder();
		iterable.forEach(m -> builder.putAll(m.definitions));

		return new ReachingDefinition(builder.build());
	}



	ReachingDefinition replace(IVariableBinding key, Expression value) {
		

		ImmutableSetMultimap.Builder<IVariableBinding, Expression> builder = ImmutableSetMultimap.builder();
		
		definitions.entries().stream()
				.filter(entry -> !entry.getKey().isEqualTo(key))
				.forEach(e -> builder.put(e.getKey(), e.getValue()));

		Collection<Expression> val;
		
		//Resolve the binding if the expression is a field/variable identifier.
		IVariableBinding variableBinding = Expressions.resolveVariableBinding(value);
						
		//Solve reference of expression if it references a variable
		if (variableBinding != null && definitions.containsKey(variableBinding)) {
			val = definitions.get(variableBinding);
		} else {
			val = Sets.newHashSet(value);
		}

		builder.putAll(key, val);

		return new ReachingDefinition(builder.build());
	}

	public Collection<Expression> get(IVariableBinding key) {
		return definitions.get(key);
	}

	public Collection<Expression> getDefinitionOf(Expression expr) {
		Collection<Expression> val;
		
		//Resolve the binding if the expression is a field/variable identifier.
		IVariableBinding variableBinding = Expressions.resolveVariableBinding(expr);
						
		//Solve reference of expression if it references a variable
		if (variableBinding != null && definitions.containsKey(variableBinding)) {
			val = definitions.get(variableBinding);
		} else {
			val = Sets.newHashSet(expr);
		}
		
		return val;
	}

	@Override
	public String toString() {
		return definitions.toString();
	}	


}
