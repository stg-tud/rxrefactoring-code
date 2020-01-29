package de.tudarmstadt.rxrefactoring.core;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import com.google.common.annotations.Beta;

import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;

/**
 * A compilation unit that can be directly rewritten.
 * 
 * @author mirko
 * 
 * @see ICompilationUnit
 * @see ASTRewrite
 *
 */
public interface IRewriteCompilationUnit extends ICompilationUnit {
	/**
	 * Accepts a visitor for this compilation units AST.
	 * 
	 * @param visitor
	 *            The visitor to use.
	 * 
	 * @see ASTNode#accept(ASTVisitor)
	 */
	public void accept(@NonNull ASTVisitor visitor);

	/**
	 * The root node of this compilation units AST. Use {@link writer()} when you
	 * want to make changes to the AST.
	 * 
	 * @return The root node of this compilation units AST. Cannot be null.
	 */
	public @NonNull ASTNode getRoot();

	/**
	 * Retrieves the {@link AST} used for this compilation unit.
	 * 
	 * @return The non-null {@link AST}.
	 */
	public @NonNull AST getAST();

	/**
	 * Retrieves the {@link ASTRewrite} used for this compilation unit.
	 * 
	 * @return The non-null {@link ASTRewrite}.
	 */
	public @NonNull ASTRewrite writer();
	
	public String getWorker();
	
	public void setWorker(String worker);

	/**
	 * Retrieves the {@link ImportRewrite} used for this compilation unit.
	 * 
	 * @return The non-null {@link ImportRewrite}.
	 */
	public @NonNull ImportRewrite imports();

	public boolean hasChanges();

	public boolean hasImportChanges();

	public boolean hasASTChanges();

	/**
	 * Replaces an AST node with another AST node. This replacement is not immediate
	 * as it does not change the underlying AST graph. * <br>
	 * <br>
	 * This method does not change the AST until
	 * {@link RewriteCompilationUnit#applyChanges(IProgressMonitor)} has been
	 * called.
	 * 
	 * 
	 * @param node
	 *            The node that should be marked for replacement.
	 * @param replacement
	 *            The new node.
	 * 
	 * @see ASTRewrite#replace(ASTNode, ASTNode,
	 *      org.eclipse.text.edits.TextEditGroup)
	 */
	default public void replace(@NonNull ASTNode node, @Nullable ASTNode replacement) {
		ASTRewrite writer = writer();
		synchronized (writer) {
			writer.replace(node, replacement, null);
		}
	}

	/**
	 * Creates a placeholder node for a true copy of the given node. This method is
	 * thread-safe.
	 * 
	 * @param node
	 *            The node to create a copy placeholder for.
	 * @return the new placeholder node
	 * 
	 * @see ASTRewrite#createCopyTarget(ASTNode)
	 */
	@SuppressWarnings({ "unchecked", "null" })
	default public @NonNull <V extends ASTNode> V copyNode(@NonNull V node) {
		if (node == null || node.getParent() == null)
			return node;

		ASTRewrite writer = writer();
		synchronized (writer) {
			return (V) writer.createCopyTarget(node);
		}
	}

	/**
	 * Returns a deep copy of the subtree of AST nodes rooted at the given node.
	 * This method is thread-safe.
	 * 
	 * @param node
	 *            the node to copy
	 * @return the copied node
	 * 
	 * @see ASTNode#copySubtree(AST, ASTNode)
	 */
	@SuppressWarnings({ "unchecked", "null" })
	default public @NonNull <V extends ASTNode> V cloneNode(@NonNull V node) {
		ASTRewrite writer = writer();
		synchronized (writer) {
			return (V) ASTNode.copySubtree(getAST(), node);
		}
	}

	/**
	 * Marks the given node for removal. This method is thread-safe.
	 * 
	 * @param node
	 *            the node being removed.
	 * 
	 * @see ASTRewrite#remove(ASTNode, org.eclipse.text.edits.TextEditGroup)
	 */
	default public void remove(@NonNull ASTNode node) {
		ASTRewrite writer = writer();
		synchronized (writer) {
			writer.remove(node, null);
		}
	}

	/**
	 * Adds an import to this compilation unit. No imports are added for types that
	 * are already known. <br>
	 * <br>
	 * This method does not change the AST until
	 * {@link RewriteCompilationUnit#applyChanges(IProgressMonitor)} has been
	 * called.
	 * 
	 * @param qualifiedTypeName
	 *            The qualified name of the type that should be imported.
	 * 
	 * @see ImportRewrite#addImport(String)
	 */
	default public void addImport(String qualifiedTypeName) {
		ImportRewrite imports = imports();
		synchronized (imports) {
			imports.addImport(qualifiedTypeName);
		}
	}

	/**
	 * Removes an import from this compilation unit. <br>
	 * <br>
	 * This method does not change the AST until
	 * {@link RewriteCompilationUnit#applyChanges(IProgressMonitor)} has been
	 * called.
	 * 
	 * @param qualifiedTypeName
	 *            The qualified name of the type that should be removed.
	 * 
	 * @see ImportRewrite#removeImport(String)
	 */
	default public void removeImport(String qualifiedTypeName) {
		ImportRewrite imports = imports();
		synchronized (imports) {
			imports.removeImport(qualifiedTypeName);
		}
	}

	/**
	 * Creates and returns a new rewriter for describing modifications to the given
	 * list property of the given node.
	 *
	 * @param node
	 *            the node
	 * @param property
	 *            the node's property; the child list property
	 * @return a new list rewriter object
	 * 
	 * @throws IllegalArgumentException
	 *             if the node or property is null, or if the node is not part of
	 *             this rewriter's AST, or if the property is not a node property,
	 *             or if the described modification is invalid
	 * 
	 * @see ASTRewrite#getListRewrite(ASTNode, ChildListPropertyDescriptor)
	 */
	default public ListRewrite getListRewrite(ASTNode node, ChildListPropertyDescriptor property) {
		return writer().getListRewrite(node, property);
	}

	/**
	 * Returns the rewritten node of the given node. If the node has not been
	 * rewritten, then it returns the original node. 
	 * 
	 * @param node
	 *            The node to check.
	 * @return The possible rewriting of the original node, or the original node.
	 */
	@Beta
	default public <T extends ASTNode> T getRewrittenNode(T node) {
		Objects.requireNonNull(node, "node can not be null.");

		StructuralPropertyDescriptor descriptor = node.getLocationInParent();
		ASTNode parent = node.getParent();

		if (parent == null || descriptor == null)
			throw new IllegalStateException("parent or descriptor are null.");

		if (descriptor instanceof ChildListPropertyDescriptor) {
			ChildListPropertyDescriptor clpd = (ChildListPropertyDescriptor) descriptor;
			ListRewrite l = writer().getListRewrite(parent, clpd);

			@SuppressWarnings("rawtypes")
			List rewritten = l.getRewrittenList();
			@SuppressWarnings("rawtypes")
			List original = l.getOriginalList();

			for (int i = 0; i < original.size(); i++) {
				if (Objects.equals(original.get(i), node)) {
					try {
						return (T) rewritten.get(i);
					} catch (IndexOutOfBoundsException e) {
						return node;
					}
				}
			}

			return node;
		} else {
			return (T) writer().get(node, descriptor);
		}
	}

	/**
	 * Returns the rewritten node of the given node. If the node has not been
	 * rewritten, then it returns the original node. If the node is located in a
	 * list, then the index of the node has to be given as well.
	 * 
	 * @param node
	 *            The node to check.
	 * @param index
	 *            The index of the enclosing list (if any).
	 * @return The possible rewriting of the original node, or the original node.
	 * 
	 * @see IRewriteCompilationUnit#getRewrittenNode(ASTNode)
	 */
	@Beta
	default public ASTNode getRewrittenNode(ASTNode node, int index) {				
		Objects.requireNonNull(node, "node can not be null.");

		StructuralPropertyDescriptor descriptor = Objects.requireNonNull(node.getLocationInParent());
		ASTNode parent = Objects.requireNonNull(node.getParent());

		
		if (descriptor instanceof ChildListPropertyDescriptor) {
			ChildListPropertyDescriptor clpd = (ChildListPropertyDescriptor) descriptor;
			ListRewrite l = writer().getListRewrite(parent, clpd);
			return (ASTNode) l.getRewrittenList().get(index);
		} else {
			return (ASTNode) writer().get(node, descriptor);
		}
	}
	
	default public Optional<TypeDeclaration> findPrimaryTypeDeclaration() {		
		ASTNode root = getRoot();
		
		final TypeDeclaration[] type = new TypeDeclaration[1];
		accept(new ASTVisitor() {
			
			@Override
			public boolean visit(TypeDeclaration node) {
				type[0] = node;
				return false;
			}
		});
		
		return Optional.ofNullable(type[0]);			
	}

	public RewriteCompilationUnit clone() throws CloneNotSupportedException;
	
}
