package de.tudarmstadt.rxrefactoring.core.internal.execution;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;


/**
 * Bundles a compilation unit together with its AST and AST rewriter.
 * 
 * @author mirko
 *
 */
public class RewriteCompilationUnit implements IRewriteCompilationUnit {

	/**
	 * The underlying compilation unit.
	 */
	private final @NonNull ICompilationUnit unit;

	/**
	 * The root node of the AST of the compilation unit.
	 */
	private final @NonNull ASTNode rootNode;

	/**
	 * The AST rewriter to be used for this compilation unit.
	 */
	private @Nullable ASTRewrite writer;

	/**
	 * The import rewriter to be used for this compilation unit.
	 */
	private @Nullable ImportRewrite imports;

	/**
	 * The AST that has been used to create the AST of this unit.
	 */
	private @Nullable AST ast;

	/**
	 * <p>Bundles a compilation unit together with its root AST node. Use
	 * {@link RewriteCompilationUnitFactory} to generate Bundled compilation units.</p>
	 * <p>This class should not be instantiated by clients.</p>
	 * 
	 * @param unit
	 *            The compilation unit.
	 * @param rootNode
	 *            The root node of the AST.
	 * 
	 * @throws NullPointerException
	 *             if any argument is null.
	 * 
	 */
	public RewriteCompilationUnit(@NonNull ICompilationUnit unit, @NonNull ASTNode rootNode) {
		Objects.requireNonNull(unit);
		Objects.requireNonNull(rootNode);

		this.unit = unit;
		this.rootNode = rootNode;
	}

	@Override
	public void accept(@NonNull ASTVisitor visitor) {
		rootNode.accept(visitor);		
	}

	@Override
	public @NonNull ASTNode getRoot() {
		return rootNode;
	}

	@Override
	@SuppressWarnings("null")
	public @NonNull AST getAST() {
		if (ast == null) {
			ast = getRoot().getAST();
		}

		return ast;
	}

	@Override
	@SuppressWarnings("null")
	public @NonNull ASTRewrite writer() {
		if (writer == null) {
			writer = ASTRewrite.create(getAST());
		}

		return writer;
	}
	
	@Override
	@SuppressWarnings("null")
	public @NonNull ImportRewrite imports() {
		if (imports == null) {
			try {
				imports = ImportRewrite.create(unit, true);
			} catch (JavaModelException e) {
				throw new IllegalStateException(e);
			}
		}

		return imports;
	}

	
	
	
	
	
	/**
	 * Checks whether this compilation unit is marked for changes in either its AST
	 * or imports.
	 * 
	 */
	@Override
	public boolean hasChanges() {
		return hasImportChanges() || hasASTChanges();
	}

	@Override
	public boolean hasImportChanges() {
		return imports != null;
	}

	@Override
	public boolean hasASTChanges() {
		return writer != null;
	}


	/**
	 * Applies the changes marked in this compilation unit and writes them to disk.
	 * 
	 * @return True, if the compilation unit did have changes.
	 * 
	 */
	protected Optional<DocumentChange> getChangedDocument()
			throws IllegalArgumentException, MalformedTreeException, BadLocationException, CoreException {

				
		// Only do something if there are changes to the compilation unit
		if (hasChanges()) {		
						
//			// Initialize the document with the old source code
			
			Document document = new Document(getSource());					
			
			MultiTextEdit root = new MultiTextEdit(); 
			
			// Apply changes to the classes AST if there are any
			if (hasASTChanges()) {
				TextEdit edit = writer().rewriteAST(document, null);
				root.addChild(edit);
			}

			// Apply changes to the classes imports if there are any
			if (hasImportChanges()) {
				TextEdit edit = imports().rewriteImports(null); // We can add a progress monitor here.
				root.addChild(edit);
			}		
			
			DocumentChange change = new RewriteChange(unit.getElementName(), document);
			change.setEdit(root);			
			return Optional.of(change);
		}

		return Optional.empty();
	}
	
	private class RewriteChange extends DocumentChange {

		public RewriteChange(String name, IDocument document) {
			super(name, document);			
		}
		
		@Override
		protected UndoEdit performEdits(final IDocument document) throws BadLocationException, MalformedTreeException {
			//TODO: Why is the original function not working? The refactoring does not change the files.
			
			//Document has changed 
			UndoEdit undo = super.performEdits(document);
			
			
//			ITextFileBufferManager fileBufferManager= FileBuffers.getTextFileBufferManager();
//			
//			ITextFileBuffer fileBuffer= fileBufferManager.getTextFileBuffer(document);
//			if (fileBuffer == null || ! fileBuffer.isSynchronizationContextRequested()) {
//				return super.performEdits(document);
//			}
//			
//			/** The lock for waiting for computation in the UI thread to complete. */
//			final Lock completionLock= new Lock();
//			final UndoEdit[] result= new UndoEdit[1];
//			final BadLocationException[] exception= new BadLocationException[1];
//			Runnable runnable= new Runnable() {
//				@Override
//				public void run() {
//					synchronized (completionLock) {
//						try {
//							result[0]= super.performEdits(document);
//						} catch (BadLocationException e) {
//							exception[0]= e;
//						} finally {
//							completionLock.fDone= true;
//							completionLock.notifyAll();
//						}
//					}
//				}
//			};
//			
//			synchronized (completionLock) {
//				fileBufferManager.execute(runnable);
//				while (! completionLock.fDone) {
//					try {
//						completionLock.wait(500);
//					} catch (InterruptedException x) {
//					}
//				}
//			}
//			
//			if (exception[0] != null) {
//				throw exception[0];
//			}
//			
//			UndoEdit undo = result[0];
							
			//UndoEdit undo = this.getEdit().apply(document);
			
			try {
				//TODO: Replace the following lines because undo functionality is not preserved...
				IBuffer buffer = unit.getBuffer();			
				buffer.setContents(document.get());			
				buffer.save(null, false);			
				return undo;
			} catch (JavaModelException e) {
				e.printStackTrace();
				throw new BadLocationException();				
			}
		}
		
	}

	@Override
	public String toString() {
		return "RewriteCompilationUnit(" + getElementName() + ")";
	}

	/*
	 * Methods from RewriteCompilationUnit
	 */
	@Override
	public IType findPrimaryType() {
		return unit.findPrimaryType();
	}

	@Override
	public IJavaElement getElementAt(int position) throws JavaModelException {
		return unit.getElementAt(position);
	}

	@Override
	public ICompilationUnit getWorkingCopy(WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
		return unit.getWorkingCopy(owner, monitor);
	}

	@Override
	public boolean exists() {
		return unit.exists();
	}

	@Override
	public IJavaElement getAncestor(int ancestorType) {
		return unit.getAncestor(ancestorType);
	}

	@Override
	public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
		return unit.getAttachedJavadoc(monitor);
	}

	@Override
	public IResource getCorrespondingResource() throws JavaModelException {
		return unit.getCorrespondingResource();
	}

	@Override
	public String getElementName() {
		return unit.getElementName();
	}

	@Override
	public int getElementType() {
		return unit.getElementType();
	}

	@Override
	public String getHandleIdentifier() {
		return unit.getHandleIdentifier();
	}

	@Override
	public IJavaModel getJavaModel() {
		return unit.getJavaModel();
	}

	@Override
	public IJavaProject getJavaProject() {
		return unit.getJavaProject();
	}

	@Override
	public IOpenable getOpenable() {
		return unit.getOpenable();
	}

	@Override
	public IJavaElement getParent() {
		return unit.getParent();
	}

	@Override
	public IPath getPath() {
		return unit.getPath();
	}

	@Override
	public IJavaElement getPrimaryElement() {
		return unit.getPrimaryElement();
	}

	@Override
	public IResource getResource() {
		return unit.getResource();
	}

	@Override
	public ISchedulingRule getSchedulingRule() {
		return unit.getSchedulingRule();
	}

	@Override
	public IResource getUnderlyingResource() throws JavaModelException {
		return unit.getUnderlyingResource();
	}

	@Override
	public boolean isReadOnly() {
		return unit.isReadOnly();
	}

	@Override
	public boolean isStructureKnown() throws JavaModelException {
		return unit.isStructureKnown();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return unit.getAdapter(adapter);
	}

	@Override
	public IJavaElement[] getChildren() throws JavaModelException {
		return unit.getChildren();
	}

	@Override
	public boolean hasChildren() throws JavaModelException {
		return unit.hasChildren();
	}

	@Override
	public void close() throws JavaModelException {
		unit.close();
	}

	@Override
	public String findRecommendedLineSeparator() throws JavaModelException {
		return unit.findRecommendedLineSeparator();
	}

	@Override
	public IBuffer getBuffer() throws JavaModelException {
		return unit.getBuffer();
	}

	@Override
	public boolean hasUnsavedChanges() throws JavaModelException {
		return unit.hasUnsavedChanges();
	}

	@Override
	public boolean isConsistent() throws JavaModelException {
		return unit.isConsistent();
	}

	@Override
	public boolean isOpen() {
		return unit.isOpen();
	}

	@Override
	public void makeConsistent(IProgressMonitor progress) throws JavaModelException {
		unit.makeConsistent(progress);
	}

	@Override
	public void open(IProgressMonitor progress) throws JavaModelException {
		unit.open(progress);

	}

	@Override
	public void save(IProgressMonitor progress, boolean force) throws JavaModelException {
		unit.save(progress, force);
	}

	@Override
	public String getSource() throws JavaModelException {
		return unit.getSource();
	}

	@Override
	public ISourceRange getSourceRange() throws JavaModelException {
		return unit.getSourceRange();
	}

	@Override
	public ISourceRange getNameRange() throws JavaModelException {
		return unit.getNameRange();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void codeComplete(int offset, org.eclipse.jdt.core.ICodeCompletionRequestor requestor) throws JavaModelException {
		unit.codeComplete(offset, requestor);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void codeComplete(int offset, org.eclipse.jdt.core.ICompletionRequestor requestor) throws JavaModelException {
		unit.codeComplete(offset, requestor);

	}

	@Override
	public void codeComplete(int offset, CompletionRequestor requestor) throws JavaModelException {
		unit.codeComplete(offset, requestor);

	}

	@Override
	public void codeComplete(int offset, CompletionRequestor requestor, IProgressMonitor monitor)
			throws JavaModelException {
		unit.codeComplete(offset, requestor, monitor);

	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void codeComplete(int offset, org.eclipse.jdt.core.ICompletionRequestor requestor, WorkingCopyOwner owner)
			throws JavaModelException {
		unit.codeComplete(offset, requestor, owner);

	}

	@Override
	public void codeComplete(int offset, CompletionRequestor requestor, WorkingCopyOwner owner)
			throws JavaModelException {
		unit.codeComplete(offset, requestor, owner);

	}

	@Override
	public void codeComplete(int offset, CompletionRequestor requestor, WorkingCopyOwner owner,
			IProgressMonitor monitor) throws JavaModelException {
		unit.codeComplete(offset, requestor, owner, monitor);

	}

	@Override
	public IJavaElement[] codeSelect(int offset, int length) throws JavaModelException {
		return unit.codeSelect(offset, length);
	}

	@Override
	public IJavaElement[] codeSelect(int offset, int length, WorkingCopyOwner owner) throws JavaModelException {
		return unit.codeSelect(offset, length, owner);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void commit(boolean force, IProgressMonitor monitor) throws JavaModelException {
		unit.commit(force, monitor);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void destroy() {
		unit.destroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public IJavaElement findSharedWorkingCopy(org.eclipse.jdt.core.IBufferFactory bufferFactory) {
		return unit.findSharedWorkingCopy(bufferFactory);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public IJavaElement getOriginal(IJavaElement workingCopyElement) {
		return unit.getOriginal(workingCopyElement);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public IJavaElement getOriginalElement() {
		return unit.getOriginalElement();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public IJavaElement getSharedWorkingCopy(IProgressMonitor monitor, org.eclipse.jdt.core.IBufferFactory factory,
			IProblemRequestor problemRequestor) throws JavaModelException {
		return unit.getSharedWorkingCopy(monitor, factory, problemRequestor);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public IJavaElement getWorkingCopy() throws JavaModelException {
		return unit.getWorkingCopy();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public IJavaElement getWorkingCopy(IProgressMonitor monitor, org.eclipse.jdt.core.IBufferFactory factory,
			IProblemRequestor problemRequestor) throws JavaModelException {
		return unit.getWorkingCopy(monitor, factory, problemRequestor);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public boolean isBasedOn(IResource resource) {
		return unit.isBasedOn(resource);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public IMarker[] reconcile() throws JavaModelException {
		return unit.reconcile();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void reconcile(boolean forceProblemDetection, IProgressMonitor monitor) throws JavaModelException {
		unit.reconcile(forceProblemDetection, monitor);

	}

	@Override
	public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean replace,
			IProgressMonitor monitor) throws JavaModelException {
		unit.copy(container, sibling, rename, replace, monitor);
	}

	@Override
	public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
		unit.delete(force, monitor);
	}

	@Override
	public void move(IJavaElement container, IJavaElement sibling, String rename, boolean replace,
			IProgressMonitor monitor) throws JavaModelException {
		unit.move(container, sibling, rename, replace, monitor);
	}

	@Override
	public void rename(String name, boolean replace, IProgressMonitor monitor) throws JavaModelException {
		unit.rename(name, replace, monitor);
	}

	@Override
	public UndoEdit applyTextEdit(TextEdit edit, IProgressMonitor monitor) throws JavaModelException {
		return unit.applyTextEdit(edit, monitor);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void becomeWorkingCopy(IProblemRequestor problemRequestor, IProgressMonitor monitor)
			throws JavaModelException {
		unit.becomeWorkingCopy(problemRequestor, monitor);
	}

	@Override
	public void becomeWorkingCopy(IProgressMonitor monitor) throws JavaModelException {
		unit.becomeWorkingCopy(monitor);

	}

	@Override
	public void commitWorkingCopy(boolean force, IProgressMonitor monitor) throws JavaModelException {
		unit.commitWorkingCopy(force, monitor);
	}

	@Override
	public IImportDeclaration createImport(String name, IJavaElement sibling, IProgressMonitor monitor)
			throws JavaModelException {
		return unit.createImport(name, sibling, monitor);
	}

	@Override
	public IImportDeclaration createImport(String name, IJavaElement sibling, int flags, IProgressMonitor monitor)
			throws JavaModelException {
		return unit.createImport(name, sibling, flags, monitor);
	}

	@Override
	public IPackageDeclaration createPackageDeclaration(String name, IProgressMonitor monitor)
			throws JavaModelException {
		return unit.createPackageDeclaration(name, monitor);
	}

	@Override
	public IType createType(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor)
			throws JavaModelException {
		return unit.createType(contents, sibling, force, monitor);
	}

	@Override
	public void discardWorkingCopy() throws JavaModelException {
		unit.discardWorkingCopy();
	}

	@Override
	public IJavaElement[] findElements(IJavaElement element) {
		return unit.findElements(element);
	}

	@Override
	public ICompilationUnit findWorkingCopy(WorkingCopyOwner owner) {
		return unit.findWorkingCopy(owner);
	}

	@Override
	public IType[] getAllTypes() throws JavaModelException {
		return unit.getAllTypes();
	}

	@Override
	public IImportDeclaration getImport(String name) {
		return unit.getImport(name);
	}

	@Override
	public IImportContainer getImportContainer() {
		return unit.getImportContainer();
	}

	@Override
	public IImportDeclaration[] getImports() throws JavaModelException {
		return unit.getImports();
	}

	@Override
	public ICompilationUnit getPrimary() {
		return unit.getPrimary();
	}

	@Override
	public WorkingCopyOwner getOwner() {
		return unit.getOwner();
	}

	@Override
	public IPackageDeclaration getPackageDeclaration(String name) {
		return unit.getPackageDeclaration(name);
	}

	@Override
	public IPackageDeclaration[] getPackageDeclarations() throws JavaModelException {
		return unit.getPackageDeclarations();
	}

	@Override
	public IType getType(String name) {
		return unit.getType(name);
	}

	@Override
	public IType[] getTypes() throws JavaModelException {
		return unit.getTypes();
	}

	@Override
	public ICompilationUnit getWorkingCopy(IProgressMonitor monitor) throws JavaModelException {
		return unit.getWorkingCopy(monitor);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public ICompilationUnit getWorkingCopy(WorkingCopyOwner owner, IProblemRequestor problemRequestor,
			IProgressMonitor monitor) throws JavaModelException {
		return unit.getWorkingCopy(owner, problemRequestor, monitor);
	}

	@Override
	public boolean hasResourceChanged() {
		return unit.hasResourceChanged();
	}

	@Override
	public boolean isWorkingCopy() {
		return unit.isWorkingCopy();
	}

	@Override
	public CompilationUnit reconcile(int astLevel, boolean forceProblemDetection, WorkingCopyOwner owner,
			IProgressMonitor monitor) throws JavaModelException {
		return unit.reconcile(astLevel, forceProblemDetection, owner, monitor);
	}

	@Override
	public CompilationUnit reconcile(int astLevel, boolean forceProblemDetection, boolean enableStatementsRecovery,
			WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
		return unit.reconcile(astLevel, forceProblemDetection, enableStatementsRecovery, owner, monitor);
	}

	@Override
	public CompilationUnit reconcile(int astLevel, int reconcileFlags, WorkingCopyOwner owner, IProgressMonitor monitor)
			throws JavaModelException {
		return unit.reconcile(astLevel, reconcileFlags, owner, monitor);
	}

	@Override
	public void restore() throws JavaModelException {
		unit.restore();
	}

}
