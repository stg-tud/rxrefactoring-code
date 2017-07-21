package de.tudarmstadt.rxrefactoring.core.writers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import de.tudarmstadt.rxrefactoring.core.RefactoringApp;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

/**
 * Description: This class is in charge of accumulating the changes
 * of all compilation units and applying them<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public class UnitWriterExecution {
	
	private final Set<UnitWriter> writers;

	public UnitWriterExecution() {
		writers = new HashSet<>();
	}

	/**
	 * Only added compilation units will be refactored. If
	 * a compilation unit is not added to this class by using
	 * this method, then the changes will be ignored
	 * 
	 * @param unit
	 *            unit to be refactored
	 */
	public synchronized void addUnitWriter(UnitWriter writer)	{
		writers.add(writer);
	}

	@Deprecated
	public synchronized void addCompilationUnit(ICompilationUnit unit) {
		throw new UnsupportedOperationException("Use addUnitWriter instead.");
	}
	
	/**
	 * Execute all changes added and saves the results.
	 * 
	 * @param progressMonitor
	 *            progress monitor
	 */
	public void execute(IProgressMonitor progressMonitor)	{
		for (UnitWriter writer : writers) {
			try	{				
				writer.applyChanges(progressMonitor);
			} catch (Exception e)	{
				Log.error( getClass(), "METHOD=executeChanges - " + writer.getUnit().getElementName(), e);
			}
		}
	}

	// ### Private Methods ###

	

	private ImportRewrite createImportWriter( ICompilationUnit unit, UnitWriter writer ) throws JavaModelException	{
		ImportRewrite importRewriter = StubUtility.createImportRewrite( unit, true );
		
		writer.getAddedImports().forEach(importRewriter::addImport);
		writer.getRemovedImports().forEach(importRewriter::removeImport);
		
		return importRewriter;
	}

	private void applyChanges( ICompilationUnit unit, ImportRewrite importRewriter, CompilationUnitChange compilationUnitChange, IProgressMonitor progressMonitor ) throws BadLocationException, CoreException
	{
		// process source code
		String sourceCode = compilationUnitChange.getCompilationUnit().getSource();

		// load document and apply changes
		Document document = new Document( sourceCode );
		compilationUnitChange.getEdit().apply( document );
		TextEdit importsEdit = importRewriter.rewriteImports( progressMonitor );
		importsEdit.apply( document );
		String newSourceCode = document.get();
		IBuffer buffer = unit.getBuffer();
		buffer.setContents( newSourceCode );

		// save changes
		if ( !RefactoringApp.isRunningForTests() ) {
			buffer.save( progressMonitor, false );
		}
	}

	
}
