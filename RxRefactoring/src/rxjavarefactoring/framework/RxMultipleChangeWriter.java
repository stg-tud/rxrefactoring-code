package rxjavarefactoring.framework;

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

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
public class RxMultipleChangeWriter
{
	private Map<ICompilationUnit, CompilationUnitChange> icuChangesMap;
	private Map<ICompilationUnit, Set<String>> icuAddedImportsMap;
	private Map<ICompilationUnit, Set<String>> icuRemovedImportsMap;
	private Map<ICompilationUnit, String> icuVsNewSourceCodeMap;
	private ImportRewrite importRewriter;

	public RxMultipleChangeWriter()
	{
		icuChangesMap = new HashMap<>();
		icuAddedImportsMap = new HashMap<>();
		icuRemovedImportsMap = new HashMap<>();
		icuVsNewSourceCodeMap = new HashMap<>();
	}

	public void addChange( String name, ICompilationUnit icu, RxSingleChangeWriter singleChangeWriter )
	{
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
					buffer.save( progressMonitor, true );
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

	private TextEdit updateSourceCode( CompilationUnitChange compilationUnitChange, TextEdit sourceCodeEdits )
	{
		TextEdit edit = compilationUnitChange.getEdit();
		if ( edit == null )
		{
			compilationUnitChange.setEdit( sourceCodeEdits );
			edit = sourceCodeEdits;
		}
		else
		{
			edit.addChild( sourceCodeEdits );
		}
		return edit;
	}

	private void updateImports( ICompilationUnit icu, RxSingleChangeWriter singleChangeWriter )
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
