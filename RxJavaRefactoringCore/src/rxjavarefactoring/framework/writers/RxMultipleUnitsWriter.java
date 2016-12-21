package rxjavarefactoring.framework.writers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import rx.Observable;
import rxjavarefactoring.RxJavaRefactoringApp;
import rxjavarefactoring.framework.utils.RxLogger;

/**
 * Description: This class is in charge of accumulating the changes
 * of all compilation units and applying them<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
// TODO: make this class thread safe
public class RxMultipleUnitsWriter
{
	private final Map<ICompilationUnit, CompilationUnitChange> icuChangesMap;
	private final Map<ICompilationUnit, Set<String>> icuAddedImportsMap;
	private final Map<ICompilationUnit, Set<String>> icuRemovedImportsMap;
	private final Map<ICompilationUnit, String> icuVsNewSourceCodeMap;
	private ImportRewrite importRewriter;

	public RxMultipleUnitsWriter()
	{
		icuChangesMap = new HashMap<>();
		icuAddedImportsMap = new HashMap<>();
		icuRemovedImportsMap = new HashMap<>();
		icuVsNewSourceCodeMap = new HashMap<>();
	}

	/**
	 * Add change to multiple change write
	 * 
	 * @param icu
	 *            target compilation unit
	 * @param singleChangeWriter
	 *            single change writer of the compilation unit
	 */
	public void addChange( ICompilationUnit icu, RxSingleUnitWriter singleChangeWriter )
	{
		String name = icu.getElementName();
		try
		{
			CompilationUnitChange compilationUnitChange = getCuChange( name, icu );
			TextEdit sourceCodeEdits = singleChangeWriter.getAstRewriter().rewriteAST();
			updateSourceCode( compilationUnitChange, sourceCodeEdits );
			updateImports( icu, singleChangeWriter );
			icuChangesMap.put( icu, compilationUnitChange );
		}
		catch ( CoreException e )
		{
			RxLogger.error( this, "addChange: " + name, e );
		}
	}

	/**
	 * Execute all changes added and saves the results.
	 * 
	 * @param progressMonitor
	 *            progress monitor
	 */
	public void executeChanges( IProgressMonitor progressMonitor )
	{
		for ( ICompilationUnit icu : icuChangesMap.keySet() )
		{
			String compilationUnitName = "";
			try
			{
				compilationUnitName = icu.getElementName();

				// process imports
				importRewriter = StubUtility.createImportRewrite( icu, true );

				Observable
						.from( icuAddedImportsMap.get( icu ) )
						.doOnNext( newImport -> importRewriter.addImport( newImport ) )
						.subscribe();

				Observable
						.from( icuRemovedImportsMap.get( icu ) )
						.doOnNext( deletedImport -> importRewriter.removeImport( deletedImport ) )
						.subscribe();

				// process source code
				CompilationUnitChange sourceCodeEdit = icuChangesMap.get( icu );
				String sourceCode = sourceCodeEdit.getCompilationUnit().getSource();

				// load document and apply changes
				Document document = new Document( sourceCode );
				sourceCodeEdit.getEdit().apply( document );
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
			catch ( Exception e )
			{
				RxLogger.error( this, "METHOD=executeChanges - " + compilationUnitName, e );
			}
		}
	}

	public Map<ICompilationUnit, String> getIcuVsNewSourceCodeMap()
	{
		return icuVsNewSourceCodeMap;
	}

	// ### Private Methods ###

	private CompilationUnitChange getCuChange( String name, ICompilationUnit icu )
	{
		CompilationUnitChange compilationUnitChange = icuChangesMap.get( icu );
		if ( compilationUnitChange == null )
		{
			compilationUnitChange = new CompilationUnitChange( name, icu );

		}
		return compilationUnitChange;
	}

	private void updateSourceCode( CompilationUnitChange compilationUnitChange, TextEdit sourceCodeEdits )
	{
		TextEdit edit = compilationUnitChange.getEdit();
		if ( edit == null )
		{
			compilationUnitChange.setEdit( sourceCodeEdits );
		}
		else
		{
			edit.addChild( sourceCodeEdits );
		}
	}

	private void updateImports( ICompilationUnit icu, RxSingleUnitWriter singleChangeWriter )
	{
		Set<String> addedImports = singleChangeWriter.getAddedImports();
		Set<String> removedImports = singleChangeWriter.getRemovedImports();

		addToMapOrUpdate( icu, addedImports, icuAddedImportsMap );
		addToMapOrUpdate( icu, removedImports, icuRemovedImportsMap );
	}

	private <T> void addToMapOrUpdate( ICompilationUnit icu, Set<String> set, Map<ICompilationUnit, Set<String>> map )
	{
		Set<String> retrievedSet = map.get( icu );
		if ( retrievedSet == null )
		{
			map.put( icu, set );
		}
		else
		{
			retrievedSet.addAll( set );
			map.put( icu, retrievedSet );
		}
	}
}
