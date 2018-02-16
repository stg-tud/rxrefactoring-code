package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.*;

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
public class AssignmentWorker extends GeneralWorker
{
	
	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units,
			de.tudarmstadt.rxrefactoring.ext.swingworker.workers.@Nullable RxCollector input,
			@NonNull WorkerSummary summary) throws Exception
	{
	Multimap<IRewriteCompilationUnit, Assignment> varDeclMap = input.getAssigmentsMap();
	int total = varDeclMap.values().size();
	Log.info( getClass(), "METHOD=refactor - Total number of <<Assignment>>: " + total );

	for (Map.Entry<IRewriteCompilationUnit, Assignment> assignmentEntry : varDeclMap.entries())
	{
		IRewriteCompilationUnit icu = assignmentEntry.getKey();
		Assignment assignment = assignmentEntry.getValue();	
		
		AST ast = assignment.getAST();

		Expression rightHandSide = assignment.getRightHandSide();
		if ( rightHandSide instanceof ClassInstanceCreation )
		{
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) rightHandSide;

			if ( ASTUtils.isClassOf( classInstanceCreation, SwingWorkerInfo.getBinaryName() ) )
			{
				Log.info( getClass(), "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
				RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
				assignment.accept( refactoringVisitor );

				Log.info( getClass(), "METHOD=refactor - Refactoring assignment in: " + icu.getElementName() );
				refactorAssignment( icu, refactoringVisitor, assignment );
			}
		}
		else if ( rightHandSide instanceof SimpleName )
		{
			Log.info( getClass(),"METHOD=refactor - Refactoring right variable name: " + icu.getElementName() );
			SimpleName simpleName = (SimpleName) rightHandSide;
			String newIdentifier = RefactoringUtils.cleanSwingWorkerName( simpleName.getIdentifier() );
			synchronized(icu) 
			{
				icu.replace(simpleName, SwingWorkerASTUtils.newSimpleName(ast, newIdentifier));
			}
		}

		Expression leftHandSide = assignment.getLeftHandSide();
		if ( leftHandSide instanceof SimpleName )
		{
			Log.info( getClass(), "METHOD=refactor - Refactoring left variable name: " + icu.getElementName() );
			SimpleName simpleName = (SimpleName) leftHandSide;
			String newIdentifier = RefactoringUtils.cleanSwingWorkerName( simpleName.getIdentifier() );
			synchronized(icu) 
			{
				icu.replace(simpleName, SwingWorkerASTUtils.newSimpleName(ast, newIdentifier));
			}
			
		}
		
		Log.info( getClass(), "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
	
		summary.addCorrect("Assignments");
	}

	return null;
	}

	private void refactorAssignment(
			IRewriteCompilationUnit icu,
			RefactoringVisitor refactoringVisitor,
			Assignment assignment )
	{
		removeSuperInvocations( refactoringVisitor );
		updateImports( icu );

		if ( refactoringVisitor.hasAdditionalFieldsOrMethods() )
		{
			refactorStatefulSwingWorker( icu, refactoringVisitor, assignment );
		}
		else
		{
			refactorStatelessSwingWorker( icu, refactoringVisitor, assignment );
		}
	}

	private void refactorStatefulSwingWorker(
			IRewriteCompilationUnit icu,
			RefactoringVisitor refactoringVisitor,
			Assignment assignment )
	{
		SimpleName swingWorkerName;
		if(assignment.getLeftHandSide() instanceof FieldAccess) 
		{
			FieldAccess fa = (FieldAccess) assignment.getLeftHandSide();
			swingWorkerName = (SimpleName) fa.getName();
		} else {
			swingWorkerName = (SimpleName) assignment.getLeftHandSide();
		}
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.getIdentifier() );
		SWSubscriberModel subscriberDto = createSWSubscriberDto( rxObserverName, icu, refactoringVisitor );
		
		//Template is called
		Map<String, Object> subscriberData = new HashMap<>();
		subscriberData.put( "model", subscriberDto );
		String subscriberTemplate = "subscriber.ftl";
		String subscriberString = TemplateUtils.processTemplate( subscriberTemplate, subscriberData );
		AST ast = assignment.getAST();
		
		// Type declaration of RxObserver is added
		TypeDeclaration typeDeclaration = TemplateVisitor.createTypeDeclarationFromText( ast, subscriberString );
		Statement referenceStatement = ASTNodes.findParent( assignment, Statement.class ).get();
		SwingWorkerASTUtils.addBefore(icu, typeDeclaration, referenceStatement);	
		
		// Assignment of new rxObserver is added
		ExpressionStatement newAssignment = SwingWorkerASTUtils.newAssignment(ast, subscriberDto.getSubscriberName(), subscriberDto.getClassName());
		Statements.addStatementBefore(icu, newAssignment, referenceStatement);
		
		// Assignment of new SwingWorker is removed
		SwingWorkerASTUtils.removeStatement(icu, assignment);

	}

	private void refactorStatelessSwingWorker(
			IRewriteCompilationUnit icu,
			RefactoringVisitor refactoringVisitor,
			Assignment assignment )
	{
		RxObservableModel observableDto = createObservableDto( icu, refactoringVisitor );
		/*
		 * Sometimes doInbackground Block needs to be refactored futher like 
		 * MainController.this.requestWorker.execute() --> MainController.this.requestRxObserver.executeObservable()
		 * Scenario: 37--KolakCC--lol-jclient : MainController.java
		 */
		observableDto.setDoInBackgroundBlock(refactorDoInBgBlock(refactoringVisitor.getDoInBackgroundBlock()));
		
		Map<String, Object> observableData = new HashMap<>();
		observableData.put( "model", observableDto );
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );
		
		AST ast = assignment.getAST();
		
		Statement observableStatement = TemplateVisitor.createSingleStatementFromText( ast, observableString );

		Statement referenceStatement = ASTNodes.findParent( assignment, Statement.class ).get();
		Statements.addStatementBefore(icu, observableStatement, referenceStatement);
		
		SimpleName swingWorkerName = null;
		if(assignment.getLeftHandSide() instanceof SimpleName) {
			swingWorkerName = (SimpleName) assignment.getLeftHandSide();
		}
		else if(assignment.getLeftHandSide() instanceof FieldAccess)
		{
			FieldAccess fa = (FieldAccess) assignment.getLeftHandSide();
			swingWorkerName = (SimpleName) fa.getName();
		}
		if (swingWorkerName != null) {
			String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.getIdentifier() );
			RxObserverModel observerDto = createObserverDto( rxObserverName, refactoringVisitor, observableDto );
			observerDto.setVariableDecl( false );

			Map<String, Object> observerData = new HashMap<>();
			observerData.put( "model", observerDto );
			String observerTemplate = "observer.ftl";

			String observerString = TemplateUtils.processTemplate( observerTemplate, observerData );
			Statement observerStatement = TemplateVisitor.createSingleStatementFromText( ast, observerString );
			Statements.addStatementBefore(icu, observerStatement, referenceStatement);

			SwingWorkerASTUtils.removeStatement(icu, assignment);	
		}
	}
	
	private String refactorDoInBgBlock(Block doInBgBlock) 
	{
		String doInBgBlockString = "";
		List<Statement> list = doInBgBlock.statements();
		StringBuilder sb = new StringBuilder();
		sb.append("{ " + System.lineSeparator());
		for(int i=0;i<list.size();i++) {
				Statement stmnt = list.get(i);
				if(stmnt instanceof ExpressionStatement) {
					ExpressionStatement exprstmnt = (ExpressionStatement)stmnt;
					if(exprstmnt.getExpression() instanceof MethodInvocation) {
						MethodInvocation methodInvocation = (MethodInvocation) exprstmnt.getExpression();
						SimpleName methodSimpleName = methodInvocation.getName();
						String newMethodName = RefactoringUtils.getNewMethodName( methodSimpleName.toString() );
						stmnt = TemplateVisitor.createSingleStatementFromText(doInBgBlock.getAST(), stmnt.toString().replace(methodSimpleName.toString(), newMethodName));
					}
				}
				sb.append(stmnt.toString() + System.lineSeparator());
		}
		sb.append("}");
		doInBgBlockString = RefactoringUtils.cleanSwingWorkerName(sb.toString());
		return doInBgBlockString;
	}
	}
	
