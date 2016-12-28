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
import utils.TemplateUtils;
import visitors.RefactoringVisitor;
import visitors.RxCollector;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class ClassInstanceCreationWorker extends GeneralWorker
{
	public ClassInstanceCreationWorker( RxCollector rxCollector )
	{
		super( rxCollector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<ClassInstanceCreation>> classInstanceMap = collector.getClassInstanceMap();
		int numUnits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numUnits );
		RxLogger.info( this, "METHOD=refactor - Total number of compilation units: " + numUnits );

		for ( Map.Entry<ICompilationUnit, List<ClassInstanceCreation>> classInsCreationEntry : classInstanceMap.entrySet() )
		{
			ICompilationUnit icu = classInsCreationEntry.getKey();

			for ( ClassInstanceCreation classInstanceCreation : classInsCreationEntry.getValue() )
			{
				if ( !isRelevant( classInstanceCreation ) )
				{
					continue;
				}

				AST ast = classInstanceCreation.getAST();
				RxSingleUnitWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, ast, getClass().getSimpleName() );

				// Collect details about the SwingWorker
				RxLogger.info( this, "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
				RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
				classInstanceCreation.accept( refactoringVisitor );

				RxLogger.info( this, "METHOD=refactor - Refactoring class instance creation in: " + icu.getElementName() );
				refactorClassInstanceCreation( icu, singleUnitWriter, refactoringVisitor, classInstanceCreation );

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
				rxMultipleUnitsWriter.addCompilationUnit( icu );

			}
			monitor.worked( 1 );
		}

		return WorkerStatus.OK;
	}

	private boolean isRelevant( ClassInstanceCreation classInstanceCreation )
	{
		Assignment assignmentParent = ASTUtil.findParent( classInstanceCreation, Assignment.class );
		VariableDeclarationStatement varDeclParent = ASTUtil.findParent( classInstanceCreation, VariableDeclarationStatement.class );
		FieldDeclaration fieldDeclParent = ASTUtil.findParent(classInstanceCreation, FieldDeclaration.class);
		// if any is not null, then another worker handles this case
		return ( assignmentParent == null && varDeclParent == null && fieldDeclParent == null);
	}

	private void refactorClassInstanceCreation(
			ICompilationUnit icu,
			RxSingleUnitWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			ClassInstanceCreation classInstanceCreation )
	{
		removeSuperInvocations( refactoringVisitor );
		updateImports( singleUnitWriter );

		if ( refactoringVisitor.hasAdditionalFieldsOrMethods() )
		{
			refactorStatefulSwingWorker( icu, singleUnitWriter, refactoringVisitor, classInstanceCreation );
		}
		else
		{
			refactorStatelessSwingWorker( icu, singleUnitWriter, refactoringVisitor, classInstanceCreation );
		}
	}

	private void refactorStatefulSwingWorker(
			ICompilationUnit icu,
			RxSingleUnitWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			ClassInstanceCreation classInstanceCreation )
	{
		String icuName = icu.getElementName();
		String rxSubscriberName = "rxSubscriber";
		SWSubscriberDto subscriberDto = createSWSubscriberDto( rxSubscriberName, icuName, refactoringVisitor );

		Map<String, Object> subscriberData = new HashMap<>();
		subscriberData.put( "dto", subscriberDto );
		String subscriberTemplate = "subscriber.ftl";

		String subscriberString = TemplateUtils.processTemplate( subscriberTemplate, subscriberData );
		AST ast = classInstanceCreation.getAST();
		TypeDeclaration typeDeclaration = ASTNodeFactory.createTypeDeclarationFromText( ast, subscriberString );

		Statement referenceStatement = ASTUtil.findParent( classInstanceCreation, Statement.class );
		singleUnitWriter.addBefore( typeDeclaration, referenceStatement );

		MethodInvocation methodInvocation = ASTUtil.findParent( classInstanceCreation, MethodInvocation.class );
		if ( methodInvocation != null )
		{
			String invocation = getInvocationWithArguments(methodInvocation);
			String newInstanceCreationString = "new " + subscriberDto.getClassName() + "()." + invocation;
			Statement newInstanceCreation = ASTNodeFactory.createSingleStatementFromText( ast, newInstanceCreationString );
			singleUnitWriter.addBefore( newInstanceCreation, referenceStatement );
		}
		else
		{
			String newInstanceCreationString = "new " + subscriberDto.getClassName() + "()";
			Statement newInstanceCreation = ASTNodeFactory.createSingleStatementFromText( ast, newInstanceCreationString );
			singleUnitWriter.addBefore( newInstanceCreation, referenceStatement );
		}

		singleUnitWriter.removeStatement( classInstanceCreation );
	}

	private void refactorStatelessSwingWorker(
			ICompilationUnit icu,
			RxSingleUnitWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			ClassInstanceCreation classInstanceCreation )
	{
		String icuName = icu.getElementName();
		RxObservableDto observableDto = createObservableDto( icuName, refactoringVisitor );

		Map<String, Object> observableData = new HashMap<>();
		observableData.put( "dto", observableDto );
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );

		AST ast = classInstanceCreation.getAST();
		Statement observableStatement = ASTNodeFactory.createSingleStatementFromText( ast, observableString );

		Statement referenceStatement = ASTUtil.findParent( classInstanceCreation, Statement.class );
		singleUnitWriter.addBefore( observableStatement, referenceStatement );

		RxObserverDto subscriberDto = createObserverDto( null, refactoringVisitor, observableDto );

		Map<String, Object> observerData = new HashMap<>();
		observerData.put( "dto", subscriberDto );
		String observerTemplate = "observer.ftl";

		String observerString = TemplateUtils.processTemplate( observerTemplate, observerData );

		MethodInvocation methodInvocation = ASTUtil.findParent( classInstanceCreation, MethodInvocation.class );
		if ( methodInvocation != null )
		{
			String invocation = getInvocationWithArguments(methodInvocation);
			String classInstanceCreationString = observerString.substring( 0, observerString.length() - 1 );
			observerString = classInstanceCreationString + "." + invocation;
		}

		Statement observerStatement = ASTNodeFactory.createSingleStatementFromText( ast, observerString );
		singleUnitWriter.addBefore( observerStatement, referenceStatement );

		singleUnitWriter.removeStatement( classInstanceCreation );
	}

	private String getInvocationWithArguments(MethodInvocation methodInvocation)
	{
		String methodName = methodInvocation.getName().toString();
		String newMethodName = getNewMethodName( methodName );

		String target = methodName + "(";
		String replacement = newMethodName + "(";

		String statement = ASTUtil.findParent( methodInvocation, Statement.class ).toString();
		String statementUpdated = statement.replace( target, replacement );
		return statementUpdated.substring( statementUpdated.indexOf( replacement ) );
	}

	private String getNewMethodName( String methodName )
	{
		String newMethodName = SwingWorkerInfo.getPublicMethodsMap().get( methodName );

		if ( newMethodName == null )
		{
			newMethodName = methodName;
		}
		return newMethodName;
	}
}
