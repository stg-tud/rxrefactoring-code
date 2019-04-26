package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public final class ASTNodes {
	
	// There can be no instance of NodeUtils.
	private ASTNodes() { }
	
	/**
	 * Find the parent of a node given the target class. If the given node is
	 * already of the target class, then this node is returned.
	 *
	 * @param node
	 *            The source node for which the parent should be found.
	 * @param target
	 *            The class of the parent node that should be found (e.g.
	 *            VariableDeclaration.class).
	 * @param <T>
	 *            The type of the node that is returned.
	 * 
	 * @return The parent node based on the target, or the given node if it is
	 *         already an instance of the given class, or an empty Optional if no matching parent
	 *         could be found.
	 */
	@SuppressWarnings({ "unchecked", "null" })
	public static @NonNull <T extends ASTNode> Optional<T> findParent(@NonNull ASTNode node, @NonNull Class<T> target) {
		Objects.requireNonNull(node, "argument 'node' was null.");
		Objects.requireNonNull(target, "argument 'target' was null.");

		ASTNode parent = node;
		while (parent != null) {
			if (target.isInstance(parent)) {
				return (Optional<T>) Optional.of(parent);
			}		
			parent = parent.getParent();
		}

		return Optional.empty();
	}
	
	/**
	 * Find the parent of a node given the target class.
	 * The search for a parent to be within the bounds of one statement.
	 * If the given node is already of the target class, then this node is returned.
	 *
	 * @param expr
	 *            The source expression for which the parent should be found.
	 * @param target
	 *            The class of the parent node that should be found (e.g.
	 *            VariableDeclaration.class).
	 * @param <T>
	 *            The type of the node that is returned.
	 * 
	 * @return The parent node based on the target, or the given node if it is
	 *         already an instance of the given class, or an empty Optional if no matching parent
	 *         could be found within a statement.
	 */
	@SuppressWarnings({ "null", "unchecked" })
	public static @NonNull <T extends ASTNode> Optional<T> findParentInStatement(@NonNull Expression expr, @NonNull Class<T> target) {
		Objects.requireNonNull(expr, "argument 'node' was null.");
		Objects.requireNonNull(target, "argument 'target' was null.");

		ASTNode parent = expr;
		while (parent != null) {
			if (target.isInstance(parent)) {
				return (Optional<T>) Optional.of(parent);
			}			
			if (parent instanceof Statement) {
				return Optional.empty();
			}
			parent = parent.getParent();
		}
		return Optional.empty();
	}
	
		
	/**
	 * Retrieves all nodes from the given AST that fulfill the given predicate.
	 * 
	 * @param root
	 *            The root node of the AST.
	 * @param predicate
	 *            The predicate to be checked.
	 * @return All encountered AST nodes {@code n} where {@code predicate.apply(n) == true}, or
	 *         an empty Multiset if no node has been found.
	 */
	public static @NonNull Multiset<ASTNode> findNodes(@NonNull ASTNode root, @NonNull Function<ASTNode, Boolean> predicate) {
		Objects.requireNonNull(root, "argument 'root' was null.");
		Objects.requireNonNull(predicate, "argument 'predicate' was null.");
		
		@SuppressWarnings("null")
		final @NonNull Multiset<ASTNode> result = HashMultiset.create();
				
		root.accept(new ASTVisitor() {
			public boolean preVisit2(ASTNode node) {
				// Log.info(getClass(), "Visit node: " + node);

				if (predicate.apply(node))
					result.add(node);

				return Objects.isNull(result);
			}
		});
		
		return result;
	}


	
	/**
	 * Retrieves the first encountered node from the given AST that fulfills the given predicate.
	 * 
	 * @param root
	 *            The root node of the AST.
	 * @param predicate
	 *            The predicate to be checked.
	 * @return the first encountered AST node {@code n} where {@code predicate.apply(n) == true}, or
	 *         an empty Optional if no node has been found.
	 */
	@SuppressWarnings("null")
	public static @NonNull Optional<ASTNode> findNode(@NonNull ASTNode root, @NonNull Function<ASTNode, Boolean> predicate) {
		Objects.requireNonNull(root, "argument 'root' was null.");
		Objects.requireNonNull(predicate, "argument 'predicate' was null.");
				
		ASTNode[] result = new ASTNode[1];
				
		root.accept(new ASTVisitor() {
			public boolean preVisit2(ASTNode node) {
				if (predicate.apply(node))
					result[0] = node;

				return result[0] == null;
			}
		});
		
		return Optional.ofNullable(result[0]);
	}

	/**
	 * Checks whether the AST contains a node that fulfills the given predicate.
	 * @param root The root node of the AST.
	 * @param predicate The predicate to be checked.
	 * @return True, if the AST contains a node {@code n} where {@code predicate.apply(n) == true}.
	 */
	public static boolean containsNode(@NonNull ASTNode root, @NonNull Function<ASTNode, Boolean> predicate) {
		return findNode(root, predicate).isPresent();
	}
}
