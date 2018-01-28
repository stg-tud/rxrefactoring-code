package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.*;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactoringUtils;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016<br>
 * Adapted to new core by Camila Gonzalez on 24/01/2018
 */
public class SingleVariableDeclWorker implements IWorker<RxCollector, Void>
{

	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable RxCollector input,
			@NonNull WorkerSummary summary) throws Exception
	{
		Multimap<IRewriteCompilationUnit, SingleVariableDeclaration> singleVarDeclMap = input.getSingleVarDeclMap();
		int total = singleVarDeclMap.values().size();
		Log.info( getClass(), "METHOD=refactor - Total number of <<SingleVariableDeclaration>>: " + total );

		for (Map.Entry<IRewriteCompilationUnit, SingleVariableDeclaration> singVarDeclEntry : singleVarDeclMap.entries())
		{
			IRewriteCompilationUnit icu = singVarDeclEntry.getKey();
			SingleVariableDeclaration singleVarDecl = singVarDeclEntry.getValue();
			
			// Get ast and writer
			AST ast = singleVarDecl.getAST();

			Log.info( getClass(), "METHOD=refactor - Refactoring single variable declaration in: " + icu.getElementName() );
			SimpleTypeVisitor visitor = new SimpleTypeVisitor();
			singleVarDecl.accept( visitor );

			for ( SimpleName simpleName : visitor.simpleNames )
			{
				if ( "SwingWorker".equals( simpleName.toString() ) )
				{
					icu.replace(simpleName, ast.newSimpleName("SWSubscriber"));
					/*
					 *  Scenario : 46--ggasoftware--indigo : variables present as an argument of some method, after renaming import should be added.
					 *  E.g. ProgressStatusDialog.java(See executeSwingWorker method argument variable)
					 */
					icu.addImport( "de.tudarmstadt.stg.rx.swingworker.SWSubscriber" );
				}
				else
				{
				if(!(simpleName.getParent() instanceof QualifiedName))
					icu.replace(simpleName, ast.newSimpleName(RefactoringUtils.cleanSwingWorkerName( simpleName.getIdentifier() )));
				}
			}

			// Add changes to the multiple compilation units write object
			Log.info( getClass(), "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
			//rxMultipleUnitsWriter.addCompilationUnit( icu );
			
			summary.addCorrect("VariableDeclarations");
		}

		return null;
	}

	private class SimpleTypeVisitor extends ASTVisitor
	{
		private List<SimpleName> simpleNames = new ArrayList<>();

		@Override
		public boolean visit( SimpleName node )
		{
			ITypeBinding type = node.resolveTypeBinding();
			if ( ASTUtils.isTypeOf( type, SwingWorkerInfo.getBinaryName() ) )
			{
				simpleNames.add( node );
			}
			return true;
		}
	}

}
