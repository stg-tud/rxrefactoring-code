package workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import domain.RxObservableDto;
import domain.RxObserverDto;
import domain.SWSubscriberDto;
import domain.SwingWorkerInfo;
import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxSingleUnitWriter;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.WorkerStatus;
import utils.RefactoringUtils;
import utils.TemplateUtils;
import visitors.RefactoringVisitor;
import visitors.RxCollector;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class VariableDeclStatementWorker extends GeneralWorker
{
	public VariableDeclStatementWorker( RxCollector rxCollector )
	{
		super( rxCollector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<VariableDeclarationStatement>> varDeclMap = collector.getVarDeclMap();
		int total = varDeclMap.values().size();
		monitor.beginTask( getClass().getSimpleName(), total );
		RxLogger.info( this, "METHOD=refactor - Total number of <<VariableDeclarationStatement>>: " + total );

		for ( Map.Entry<ICompilationUnit, List<VariableDeclarationStatement>> varDelcEntry : varDeclMap.entrySet() )
		{
			ICompilationUnit icu = varDelcEntry.getKey();

			for ( VariableDeclarationStatement varDeclStatement : varDelcEntry.getValue() )
			{
				AST ast = varDeclStatement.getAST();
				RxSingleUnitWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, ast, getClass().getSimpleName() );

				VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDeclStatement.fragments().get( 0 );
				Expression initializer = fragment.getInitializer();
				if ( initializer instanceof ClassInstanceCreation )
				{
					ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) initializer;

					if ( ASTUtil.isClassOf( classInstanceCreation, SwingWorkerInfo.getBinaryName() ) )
					{
						RxLogger.info( this, "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
						RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
						varDeclStatement.accept( refactoringVisitor );

						RxLogger.info( this, "METHOD=refactor - Refactoring variable declaration statement in: " + icu.getElementName() );
						refactorVarDecl( icu, singleUnitWriter, refactoringVisitor, varDeclStatement, fragment );
					}
					else
					{
						RxLogger.info( this, "METHOD=refactor - Refactoring variable name: " + icu.getElementName() );
						SimpleName simpleName = fragment.getName();
						String newIdentifier = RefactoringUtils.cleanSwingWorkerName( simpleName.getIdentifier() );
						singleUnitWriter.replaceSimpleName( simpleName, newIdentifier );
					}
				}
				else
				{
					// change type
					Type type = varDeclStatement.getType();
					if (type instanceof ParameterizedType)
					{
						type = ((ParameterizedType) type).getType();
					}

					if (ASTUtil.isClassOf(type, SwingWorkerInfo.getBinaryName()))
					{
						singleUnitWriter.replaceType((SimpleType) type, "SWSubscriber");
					}

					String newVarName = RefactoringUtils.cleanSwingWorkerName(fragment.getName().getIdentifier());
					singleUnitWriter.replaceSimpleName(fragment.getName(), newVarName);

					SimpleName assignedVarSimpleName = (SimpleName) fragment.getInitializer();
					String newAssignedVarName = RefactoringUtils.cleanSwingWorkerName((assignedVarSimpleName).getIdentifier());
					singleUnitWriter.replaceSimpleName(assignedVarSimpleName, newAssignedVarName);
				}

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
				rxMultipleUnitsWriter.addCompilationUnit( icu );
			}
			monitor.worked( 1 );
		}

		return WorkerStatus.OK;
	}

	private void refactorVarDecl(
			ICompilationUnit icu,
			RxSingleUnitWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			VariableDeclarationStatement varDeclStatement,
			VariableDeclarationFragment fragment )
	{
		removeSuperInvocations( refactoringVisitor );
		updateImports( singleUnitWriter );

		if ( refactoringVisitor.hasAdditionalFieldsOrMethods() )
		{
			refactorStatefulSwingWorker( icu, singleUnitWriter, refactoringVisitor, varDeclStatement, fragment );
		}
		else
		{
			refactorStatelessSwingWorker( icu, singleUnitWriter, refactoringVisitor, varDeclStatement, fragment );
		}
	}

	private void refactorStatefulSwingWorker(
			ICompilationUnit icu,
			RxSingleUnitWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			VariableDeclarationStatement varDeclStatement,
			VariableDeclarationFragment fragment )
	{
		String icuName = icu.getElementName();
		SimpleName swingWorkerName = fragment.getName();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.getIdentifier() );
		SWSubscriberDto subscriberDto = createSWSubscriberDto( rxObserverName, icuName, refactoringVisitor );

		Map<String, Object> subscriberData = new HashMap<>();
		subscriberData.put( "dto", subscriberDto );
		String subscriberTemplate = "subscriber.ftl";

		String subscriberString = TemplateUtils.processTemplate( subscriberTemplate, subscriberData );
		AST ast = varDeclStatement.getAST();
		TypeDeclaration typeDeclaration = ASTNodeFactory.createTypeDeclarationFromText( ast, subscriberString );

		singleUnitWriter.addBefore( typeDeclaration, varDeclStatement );

		String newVarDeclStatementString = "SWSubscriber<" + subscriberDto.getResultType() + ", " + subscriberDto.getProcessType() + "> " +
				subscriberDto.getSubscriberName() + " = new " + subscriberDto.getClassName() + "()";
		Statement newVarDeclStatement = ASTNodeFactory.createSingleStatementFromText( ast, newVarDeclStatementString );
		singleUnitWriter.addBefore( newVarDeclStatement, varDeclStatement );

		singleUnitWriter.removeStatement( varDeclStatement );
	}

	private void refactorStatelessSwingWorker(
			ICompilationUnit icu,
			RxSingleUnitWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			VariableDeclarationStatement varDeclStatement,
			VariableDeclarationFragment fragment )
	{
		String icuName = icu.getElementName();
		RxObservableDto observableDto = createObservableDto( icuName, refactoringVisitor );

		Map<String, Object> observableData = new HashMap<>();
		observableData.put( "dto", observableDto );
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );

		AST ast = varDeclStatement.getAST();
		Statement observableStatement = ASTNodeFactory.createSingleStatementFromText( ast, observableString );

		singleUnitWriter.addBefore( observableStatement, varDeclStatement );

		SimpleName swingWorkerName = fragment.getName();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.getIdentifier() );
		RxObserverDto subscriberDto = createObserverDto( rxObserverName, refactoringVisitor, observableDto );
		subscriberDto.setVariableDecl( true );

		Map<String, Object> observerData = new HashMap<>();
		observerData.put( "dto", subscriberDto );
		String observerTemplate = "observer.ftl";

		String observerString = TemplateUtils.processTemplate( observerTemplate, observerData );
		Statement observerStatement = ASTNodeFactory.createSingleStatementFromText( ast, observerString );
		singleUnitWriter.addBefore( observerStatement, varDeclStatement );

		singleUnitWriter.removeStatement( varDeclStatement );
	}
}
