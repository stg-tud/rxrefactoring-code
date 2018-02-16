package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
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

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.RxObservableModel;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.RxObserverModel;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SWSubscriberModel;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.Statements;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactoringUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.SwingWorkerASTUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.TemplateUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.RefactoringVisitor;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.TemplateVisitor;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016<br>
 * Adapted to new core by Camila Gonzalez on 24/01/2018
 */
public class ClassInstanceCreationWorker extends GeneralWorker
{

	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units,
			de.tudarmstadt.rxrefactoring.ext.swingworker.workers.@Nullable RxCollector input,
			@NonNull WorkerSummary summary) throws Exception 
	{
		Multimap<IRewriteCompilationUnit, ClassInstanceCreation> classInstanceMap = input.getClassInstanceMap();
		int total = classInstanceMap.values().size();
		Log.info( getClass(), "METHOD=refactor - Total number of <<ClassInstanceCreation>>: " + total );

		for (Map.Entry<IRewriteCompilationUnit, ClassInstanceCreation> classInsCreationEntry : classInstanceMap.entries())
		{
			IRewriteCompilationUnit icu = classInsCreationEntry.getKey();
			ClassInstanceCreation classInstanceCreation = classInsCreationEntry.getValue();
			if ( !isRelevant( classInstanceCreation ) )
			{
				continue;
			}
			
			// Collect details about the SwingWorker
			Log.info( getClass(), "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
			RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
			classInstanceCreation.accept( refactoringVisitor );

			Log.info( getClass(), "METHOD=refactor - Refactoring class instance creation in: " + icu.getElementName() );
			refactorClassInstanceCreation( icu, refactoringVisitor, classInstanceCreation );

			// Add changes to the multiple compilation units write object
			Log.info( getClass(), "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
			
			summary.addCorrect("ClassInstances");
		}

		return null;
	}

	private boolean isRelevant( ClassInstanceCreation classInstanceCreation )
	{
		Optional<Assignment> assignmentParent = ASTNodes.findParent( classInstanceCreation, Assignment.class );
		Optional<VariableDeclarationStatement> varDeclParent = ASTNodes.findParent( classInstanceCreation, VariableDeclarationStatement.class );
		Optional<FieldDeclaration> fieldDeclParent = ASTNodes.findParent( classInstanceCreation, FieldDeclaration.class );
		
		// if any is present, then another worker handles this case
		return (!assignmentParent.isPresent() && !varDeclParent.isPresent() && !fieldDeclParent.isPresent() );
	}

	private void refactorClassInstanceCreation(
			IRewriteCompilationUnit icu,
			RefactoringVisitor refactoringVisitor,
			ClassInstanceCreation classInstanceCreation )
	{
		// Check if the class instance creation corresponds to a subclass of SwingWorker
		Optional<MethodInvocation> methodInvocation = ASTNodes.findParent( classInstanceCreation, MethodInvocation.class );
		if ( methodInvocation.isPresent() && methodInvocation.get().getExpression() instanceof ClassInstanceCreation )
		{
			if ( ASTUtils.isSubclassOf( classInstanceCreation, SwingWorkerInfo.getBinaryName(), false ) )
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
								synchronized(icu) 
								{
									icu.replace(mdInner.getName(), SwingWorkerASTUtils.newSimpleName(icu.getAST(), "getRxObservable"));
								}
							}
						}
					}
				}
				// Refactor only the method name
				SimpleName methodInvocationName = methodInvocation.get().getName();
				String newMethodName = RefactoringUtils.getNewMethodName( methodInvocationName.getIdentifier() );
				synchronized(icu) 
				{
					icu.replace(methodInvocationName, SwingWorkerASTUtils.newSimpleName(icu.getAST(), newMethodName));
				}
				return;
			}
		}

		removeSuperInvocations( refactoringVisitor );
		updateImports( icu );

		if ( refactoringVisitor.hasAdditionalFieldsOrMethods() )
		{
			refactorStatefulSwingWorker( icu, refactoringVisitor, classInstanceCreation );
		}
		else
		{
			refactorStatelessSwingWorker( icu, refactoringVisitor, classInstanceCreation );
		}
	}

	private void refactorStatefulSwingWorker(
			IRewriteCompilationUnit icu,
			RefactoringVisitor refactoringVisitor,
			ClassInstanceCreation classInstanceCreation )
	{
		String rxSubscriberName = "rxSubscriber";
		SWSubscriberModel subscriberDto = createSWSubscriberDto( rxSubscriberName, icu, refactoringVisitor );

		Map<String, Object> subscriberData = new HashMap<>();
		subscriberData.put( "model", subscriberDto );
		String subscriberTemplate = "subscriber.ftl";

		String subscriberString = TemplateUtils.processTemplate( subscriberTemplate, subscriberData );
		AST ast = classInstanceCreation.getAST();
		TypeDeclaration typeDeclaration = TemplateVisitor.createTypeDeclarationFromText( ast, subscriberString );

		Optional<Statement> referenceStatement = ASTNodes.findParent( classInstanceCreation, Statement.class );
		
		SwingWorkerASTUtils.addBefore(icu, typeDeclaration, referenceStatement.get());

		Optional<MethodInvocation> methodInvocation = ASTNodes.findParent( classInstanceCreation, MethodInvocation.class );
		if ( methodInvocation.isPresent())
		{
			String invocation = getInvocationWithArguments( methodInvocation.get() );
			String newInstanceCreationString = "new " + subscriberDto.getClassName() + "()." + invocation;
			Statement newInstanceCreation = TemplateVisitor.createSingleStatementFromText( ast, newInstanceCreationString );
			Statements.addStatementBefore(icu, newInstanceCreation, referenceStatement.get());
		}
		else
		{
			String newInstanceCreationString = "new " + subscriberDto.getClassName() + "()";
			Statement newInstanceCreation = TemplateVisitor.createSingleStatementFromText( ast, newInstanceCreationString );
			Statements.addStatementBefore(icu, newInstanceCreation, referenceStatement.get());
		}

		SwingWorkerASTUtils.removeStatement(icu, classInstanceCreation);
	}

	private void refactorStatelessSwingWorker(
			IRewriteCompilationUnit icu,
			RefactoringVisitor refactoringVisitor,
			ClassInstanceCreation classInstanceCreation )
	{
		RxObservableModel observableDto = createObservableDto( icu, refactoringVisitor );

		Map<String, Object> observableData = new HashMap<>();
		observableData.put( "model", observableDto );
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );

		AST ast = classInstanceCreation.getAST();
		
		Statement observableStatement = TemplateVisitor.createSingleStatementFromText( ast, observableString );
		
		Statement referenceStatement = ASTNodes.findParent( classInstanceCreation, Statement.class ).get();
		Statements.addStatementBefore(icu, observableStatement, referenceStatement);

		RxObserverModel subscriberDto = createObserverDto( null, refactoringVisitor, observableDto );

		Map<String, Object> observerData = new HashMap<>();
		observerData.put( "model", subscriberDto );
		String observerTemplate = "observer.ftl";

		String observerString = TemplateUtils.processTemplate( observerTemplate, observerData );

		Optional<MethodInvocation> methodInvocation = ASTNodes.findParent( classInstanceCreation, MethodInvocation.class );
		if ( methodInvocation.isPresent())
		{
			String invocation = getInvocationWithArguments( methodInvocation.get() );
			String classInstanceCreationString = observerString.substring( 0, observerString.length() - 1 );
			observerString = classInstanceCreationString + "." + invocation;
		}

		if (referenceStatement.toString().indexOf("return") == 0)
		{
			observerString = "return " + observerString;
		}

		Statement observerStatement = TemplateVisitor.createSingleStatementFromText( ast, observerString );
		Statements.addStatementBefore(icu, observerStatement, referenceStatement);

		SwingWorkerASTUtils.removeStatement(icu, classInstanceCreation);
	}

	private String getInvocationWithArguments( MethodInvocation methodInvocation )
	{
		String methodName = methodInvocation.getName().toString();
		String newMethodName = getNewMethodName( methodName );

		String target = methodName + "(";
		String replacement = newMethodName + "(";

		String statement = ASTNodes.findParent( methodInvocation, Statement.class ).get().toString();
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
