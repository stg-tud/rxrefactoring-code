package de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions;

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.Name;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Sets;

public class ReachingDefinition {
	private ImmutableMultimap<String, Expression> definitions;

	ReachingDefinition(ImmutableMultimap<String, Expression> defintions) {
		this.definitions = defintions;
	}

	static ReachingDefinition empty() {
		ReachingDefinition result = new ReachingDefinition(ImmutableMultimap.of());
		return result;
	}

	static ReachingDefinition merge(Iterable<ReachingDefinition> iterable) {
		ImmutableMultimap.Builder<String, Expression> builder = ImmutableMultimap.builder();
		iterable.forEach(m -> builder.putAll(m.definitions));

		return new ReachingDefinition(builder.build());
	}



	ReachingDefinition replace(Name key, Expression value) {
		String id = nameAsString(key);

		ImmutableMultimap.Builder<String, Expression> builder = ImmutableMultimap.builder();
		definitions.entries().stream()
				.filter(entry -> !entry.getKey().equals(id))
				.forEach(e -> builder.put(e.getKey(), e.getValue()));

		Collection<Expression> val;
		String valId;

		if (value instanceof Name && definitions.containsKey(valId = nameAsString((Name) value))) {
			val = definitions.get(valId);
		} else {
			val = Sets.newHashSet(value);
		}

		builder.putAll(id, val);

		return new ReachingDefinition(builder.build());
	}

	public Collection<Expression> get(Name key) {
		return definitions.get(nameAsString(key));
	}

	public Collection<Expression> getDefinitionOf(Expression expr) {
		Collection<Expression> val;
		String valId;
		if (expr instanceof Name && definitions.containsKey(valId = nameAsString((Name) expr))) {
			val = definitions.get(valId);
		} else {
			val = Sets.newHashSet(expr);
		}

		return val;
	}

	@Override
	public String toString() {
		return definitions.toString();
	}

	private String nameAsString(Name name) {
		IBinding binding = name.resolveBinding();

		if (binding == null)
			return name.getFullyQualifiedName();
		else
			return binding.getName();
	}


}
