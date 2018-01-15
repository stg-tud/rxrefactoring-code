package workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import domain.RxObservableModel;
import domain.RxObserverModel;
import domain.SWSubscriberModel;
import domain.SwingWorkerInfo;
import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.WorkerStatus;
import utils.RefactoringUtils;
import utils.TemplateUtils;
import visitors.RefactoringVisitor;
import visitors.RxCollector;
import writer.RxSwingWorkerWriter;

/**
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
		int total = classInstanceMap.values().size();
		monitor.beginTask( getClass().getSimpleName(), total );
		RxLogger.info( this, "METHOD=refactor - Total number of <<ClassInstanceCreation>>: " + total );

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
				RxSwingWorkerWriter rxSwingWorkerWriter = new RxSwingWorkerWriter(icu, ast, getClass().getSimpleName());
				RxSwingWorkerWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, rxSwingWorkerWriter );

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
		FieldDeclaration fieldDeclParent = ASTUtil.findParent( classInstanceCreation, FieldDeclaration.class );
		// if any is not null, then another worker handles this case
		return (assignmentParent == null && varDeclParent == null && fieldDeclParent == null );
	}

	private void refactorClassInstanceCreation(
			ICompilationUnit icu,
			RxSwingWorkerWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			ClassInstanceCreation classInstanceCreation )
	{
		// Check if the class instance creation corresponds to a subclass of SwingWorker
		MethodInvocation methodInvocation = ASTUtil.findParent( classInstanceCreation, MethodInvocation.class );
		if ( methodInvocation != null && methodInvocation.getExpression() instanceof ClassInstanceCreation )
		{
			if ( ASTUtil.isSubclassOf( classInstanceCreation, SwingWorkerInfo.getBinaryName(), false ) )
			{
				/*
				 *  Abstract class : For anonymous swingworker classes, we need to replace doInBackground method name with getRxObservable
				 *  Scenario: 37--KolakCC--lol-jclient : ProfileController.java StoreController.java
				 */
				if(classInstanceCreation.getAnonymousClassDeclaration() != null) {
					List childlist = classInstanceCreation.getAnonymousClassDeclaration().bodyDeclarations();
					for(int k=0;k<childlist.size();k++) {
						if(childlist.get(k) instanceof MethodDeclaration) {
							MethodDeclaration mdInner = (MethodDeclaration)childlist.get(k);
							if(mdInner.getName() != null && mdInner.getName().getIdentifier().equals("doInBackground")) {
								singleUnitWriter.replaceSimpleName(mdInner.getName(), "getRxObservable");	
							}
						}
					}
				}
				// Refactor only the method name
				SimpleName methodInvocationName = methodInvocation.getName();
				String newMethodName = RefactoringUtils.getNewMethodName( methodInvocationName.getIdentifier() );
				singleUnitWriter.replaceSimpleName( methodInvocationName, newMethodName );
				return;
			}
		}

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
			RxSwingWorkerWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			ClassInstanceCreation classInstanceCreation )
	{
		String icuName = icu.getElementName();
		String rxSubscriberName = "rxSubscriber";
		SWSubscriberModel subscriberDto = createSWSubscriberDto( rxSubscriberName, icuName, refactoringVisitor );

		Map<String, Object> subscriberData = new HashMap<>();
		subscriberData.put( "model", subscriberDto );
		String subscriberTemplate = "subscriber.ftl";

		String subscriberString = TemplateUtils.processTemplate( subscriberTemplate, subscriberData );
		AST ast = classInstanceCreation.getAST();
		TypeDeclaration typeDeclaration = ASTNodeFactory.createTypeDeclarationFromText( ast, subscriberString );

		Statement referenceStatement = ASTUtil.findParent( classInstanceCreation, Statement.class );
		singleUnitWriter.addBefore( typeDeclaration, referenceStatement );

		MethodInvocation methodInvocation = ASTUtil.findParent( classInstanceCreation, MethodInvocation.class );
		if ( methodInvocation != null )
		{
			String invocation = getInvocationWithArguments( methodInvocation );
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
			RxSwingWorkerWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			ClassInstanceCreation classInstanceCreation )
	{
		String icuName = icu.getElementName();
		RxObservableModel observableDto = createObservableDto( icuName, refactoringVisitor );

		Map<String, Object> observableData = new HashMap<>();
		observableData.put( "model", observableDto );
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );

		AST ast = classInstanceCreation.getAST();
		Statement observableStatement = ASTNodeFactory.createSingleStatementFromText( ast, observableString );

		Statement referenceStatement = ASTUtil.findParent( classInstanceCreation, Statement.class );
		singleUnitWriter.addBefore( observableStatement, referenceStatement );

		RxObserverModel subscriberDto = createObserverDto( null, refactoringVisitor, observableDto );

		Map<String, Object> observerData = new HashMap<>();
		observerData.put( "model", subscriberDto );
		String observerTemplate = "observer.ftl";

		String observerString = TemplateUtils.processTemplate( observerTemplate, observerData );

		MethodInvocation methodInvocation = ASTUtil.findParent( classInstanceCreation, MethodInvocation.class );
		if ( methodInvocation != null )
		{
			String invocation = getInvocationWithArguments( methodInvocation );
			String classInstanceCreationString = observerString.substring( 0, observerString.length() - 1 );
			observerString = classInstanceCreationString + "." + invocation;
		}

		if (referenceStatement.toString().indexOf("return") == 0)
		{
			observerString = "return " + observerString;
		}

		Statement observerStatement = ASTNodeFactory.createSingleStatementFromText( ast, observerString );
		singleUnitWriter.addBefore( observerStatement, referenceStatement );

		singleUnitWriter.removeStatement( classInstanceCreation );
	}

	private String getInvocationWithArguments( MethodInvocation methodInvocation )
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