package de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Name;

public class UseDef {

	private final ImmutableSetMultimap<Expression, Use> map;

	UseDef(final ImmutableSetMultimap<Expression, Use> map) {
		this.map = map;
	}

	public Set<Use> getUses(Expression definition) {
		return map.get(definition);
	}

	public SetMultimap<Expression, Use> asMap() {
		return map;
	}

	static Builder empty() {
		return new Builder();
	}

	Builder builder() {
		return new Builder(this);
	}
	
	
	@Override
	public String toString() {
		return map.asMap().entrySet().stream()
				.map(e -> new HashMap.SimpleImmutableEntry<>(
						e.getKey().toString().replace("\n", ""),
						e.getValue().stream()
								.map(Use::toString)
								.collect(Collectors.joining(", ", "[", "]"))))
				.map(e -> e.getKey() + " is used as " + e.getValue())
				.collect(Collectors.joining("\n", "<UseDef>\n", "\n</UseDef>"));
	}

	static class Builder {

		private ImmutableSetMultimap.Builder<Expression, Use> mapBuilder;

		Builder(UseDef old) {
			this.mapBuilder = ImmutableSetMultimap.builder();
			this.mapBuilder.putAll(old.map);
		}

		Builder() {
			this.mapBuilder = ImmutableSetMultimap.builder();
		}

		public Builder addUse(Expression def, Use use) {
			if (use == null || def == null)
				System.out.println("test");
			
			mapBuilder.put(def, use);
			return this;
		}

		public Builder addAll(UseDef useDef) {
			mapBuilder.putAll(useDef.map);
			return this;
		}

		public Builder addAll(Builder builder) {
			this.mapBuilder.putAll(builder.build().map);
			return this;
		}

		public UseDef build() {
			return new UseDef(mapBuilder.build());
		}
	}

	public static class Use {

		enum Kind {
			METHOD_INVOCATION, METHOD_PARAMETER, RETURN, FIELD_ASSIGN
		}

		private final Kind kind;
		@Nullable
		private final Name name;
		private final ASTNode op;

		Use(Kind kind, @Nullable Name name, ASTNode op) {
			this.kind = kind;
			this.name = name;
			this.op = op;
		}

		public Kind getKind() {
			return kind;
		}

		@Nullable
		public Name getName() {
			return name;
		}

		public ASTNode getOp() {
			return op;
		}


		@Override
		public String toString() {
			return getKind().name() + "(" + op.toString().replaceAll("\n", "") + ")"
					+ (name == null ? "" : " as '" + name.getFullyQualifiedName() + "'");
		}
	}
}
