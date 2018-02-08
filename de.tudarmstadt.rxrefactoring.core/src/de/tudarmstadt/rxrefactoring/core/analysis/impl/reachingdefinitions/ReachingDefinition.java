package de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions;

import java.util.Collection;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.Name;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class ReachingDefinition {
	private Multimap<String, Expression> definitions;
	
	ReachingDefinition(Multimap<String, Expression> defintions) {
		this.definitions = defintions;
	}
	
	ReachingDefinition setImmutable() {
		definitions = Multimaps.unmodifiableMultimap(definitions);
		return this;
	}
	
	static ReachingDefinition empty() {
		ReachingDefinition result = new ReachingDefinition(ImmutableMultimap.of());
		return result;
	}
	
	static ReachingDefinition merge(Iterable<ReachingDefinition> iterable) {
		SetMultimap<String, Expression> rmap = Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());			
		iterable.forEach(m -> rmap.putAll(m.definitions));			
		
		return new ReachingDefinition(rmap);
	}
	
	static ReachingDefinition from(ReachingDefinition other) {
		SetMultimap<String, Expression> rmap = Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
		rmap.putAll(other.definitions);
				
		return new ReachingDefinition(rmap);
	}
	
	
	ReachingDefinition replace(Name key, Expression value) {
		String id = nameAsString(key);
		definitions.removeAll(id);
		
		Collection<Expression> val;
		String valId;
		
		if (value instanceof Name && definitions.containsKey(valId = nameAsString((Name) value))) {
			val = definitions.get(valId); 				
		} else {
			val = Sets.newHashSet(value);
		}
		
		definitions.putAll(id, val);
		
		return this;
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