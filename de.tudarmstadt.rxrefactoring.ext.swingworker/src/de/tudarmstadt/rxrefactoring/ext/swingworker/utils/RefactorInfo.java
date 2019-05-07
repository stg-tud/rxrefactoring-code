package de.tudarmstadt.rxrefactoring.ext.swingworker.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.Lists;

/**
 * This class contains information about which types should be refactored.
 * 
 * @author mirko
 *
 */
public class RefactorInfo {

	private final List<TypeInfo> types = Lists.newLinkedList();

	private class TypeInfo {
		private final TypeDeclaration decl;
		private final ITypeBinding binding;

		/**
		 * Specifies manually whether this type can be refactored.
		 */
		private final boolean isRefactorable;

		public TypeInfo(TypeDeclaration decl, boolean isRefactorable) {
			this.decl = decl;
			this.binding = decl.resolveBinding();

			this.isRefactorable = isRefactorable;
		}

		public TypeInfo(TypeDeclaration decl) {
			this(decl, true);
		}

		public boolean shouldBeRefactored() {
			if (!isRefactorable)
				return false;

			return true;
		}

		public String toString() {
			return decl.getName() + ": " + shouldBeRefactored();
		}

	}

	public void add(TypeDeclaration decl) {
		add(decl, true);
	}

	public void add(TypeDeclaration decl, boolean isRefactorable) {
		types.add(new TypeInfo(decl, isRefactorable));
	}

	public boolean shouldBeRefactored(Type type) {
		return shouldBeRefactored(type.resolveBinding());
	}

	public boolean shouldBeRefactored(Expression expr) {
		return shouldBeRefactored(expr.resolveTypeBinding());
	}

	public boolean shouldBeRefactored(ITypeBinding binding) {
		if (binding == null)
			return false;

		boolean res = types.stream()
				// TODO How to sensibly compare type bindings?
				.filter(info -> binding.getQualifiedName().equals(info.binding.getQualifiedName()))
				.findFirst()
				.map(info -> info.shouldBeRefactored()).orElse(false);
		
		return res;
	}

	public String toString() {
		return types.toString();
	}
}
