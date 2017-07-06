package de.tudarmstadt.refactoringrx.core.writers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

import de.tudarmstadt.refactoringrx.core.RxJavaRefactoringApp;
import de.tudarmstadt.refactoringrx.core.utils.RxLogger;
import rx.Observable;

/**
 * Description: This class is in charge of accumulating the changes
 * of all compilation units and applying them<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public class RxMultipleUnitsWriter
{
	private final Map<ICompilationUnit, String> icuVsNewSourceCodeMap;
	private final Set<ICompilationUnit> compilationUnits;

	public RxMultipleUnitsWriter()
	{
		icuVsNewSourceCodeMap = new HashMap<>();
		compilationUnits = new HashSet<>();
	}

	/**
	 * Only added compilation units will be refactored. If
	 * a compilation unit is not added to this class by using
	 * this method, then the changes will be ignored
	 * 
	 * @param icu
	 *            unit to be refactored
	 */
	public synchronized void addCompilationUnit( ICompilationUnit icu )
	{
		compilationUnits.add( icu );
	}

	/**
	 * Execute all changes added and saves the results.
	 * 
	 * @param progressMonitor
	 *            progress monitor
	 */
	public void executeChanges( IProgressMonitor progressMonitor )
	{
		for ( ICompilationUnit icu : compilationUnits )
		{
			String compilationUnitName = icu.getElementName();
			try
			{
				RxSingleUnitWriter singleUnitWriter = RxSingleUnitWriterMapHolder.findSingleUnitWriter( icu );
				CompilationUnitChange compilationUnitChange = createCompilationUnitChange( icu, singleUnitWriter );
				ImportRewrite importRewriter = createImportWriter( icu, singleUnitWriter );
				applyChanges( icu, importRewriter, compilationUnitChange, progressMonitor );
			}
			catch ( Exception e )
			{
				RxLogger.error( this, "METHOD=executeChanges - " + compilationUnitName, e );
			}
		}
	}

	/**
	 *
	 * @return Map containing the compilation unit instances and the refactored source code as a string
	 */

	public Map<ICompilationUnit, String> getIcuVsNewSourceCodeMap()
	{
		return icuVsNewSourceCodeMap;
	}

	// ### Private Methods ###

	private CompilationUnitChange createCompilationUnitChange( ICompilationUnit icu, RxSingleUnitWriter singleUnitWriter ) throws JavaModelException
	{
		String name = icu.getElementName();
		CompilationUnitChange compilationUnitChange = new CompilationUnitChange( name, icu );
		TextEdit sourceCodeEdits = singleUnitWriter.getAstRewriter().rewriteAST();
		compilationUnitChange.setEdit( sourceCodeEdits );
		return compilationUnitChange;
	}

	private ImportRewrite createImportWriter( ICompilationUnit icu, RxSingleUnitWriter singleUnitWriter ) throws JavaModelException
	{
		ImportRewrite importRewriter = StubUtility.createImportRewrite( icu, true );
		Observable
				.from( singleUnitWriter.getAddedImports() )
				.doOnNext( newImport -> importRewriter.addImport( newImport ) )
				.subscribe();

		Observable
				.from( singleUnitWriter.getRemovedImports() )
				.doOnNext( deletedImport -> importRewriter.removeImport( deletedImport ) )
				.subscribe();
		return importRewriter;
	}

	private void applyChanges( ICompilationUnit icu, ImportRewrite importRewriter, CompilationUnitChange compilationUnitChange, IProgressMonitor progressMonitor ) throws BadLocationException, CoreException
	{
		// process source code
		String sourceCode = compilationUnitChange.getCompilationUnit().getSource();

		// load document and apply changes
		Document document = new Document( sourceCode );
		compilationUnitChange.getEdit().apply( document );
		TextEdit importsEdit = importRewriter.rewriteImports( progressMonitor );
		importsEdit.apply( document );
		String newSourceCode = document.get();
		icuVsNewSourceCodeMap.put( icu, newSourceCode );
		IBuffer buffer = icu.getBuffer();
		buffer.setContents( newSourceCode );

		// save changes
		if ( !RxJavaRefactoringApp.isRunningForTests() )
		{
			buffer.save( progressMonitor, false );
		}
	}
}
