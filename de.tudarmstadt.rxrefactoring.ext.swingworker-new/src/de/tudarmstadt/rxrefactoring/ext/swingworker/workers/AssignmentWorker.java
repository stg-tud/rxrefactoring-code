package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			icu.replace(simpleName, ast.newSimpleName(newIdentifier));
		}

		Expression leftHandSide = assignment.getLeftHandSide();
		if ( leftHandSide instanceof SimpleName )
		{
			Log.info( getClass(), "METHOD=refactor - Refactoring left variable name: " + icu.getElementName() );
			SimpleName simpleName = (SimpleName) leftHandSide;
			String newIdentifier = RefactoringUtils.cleanSwingWorkerName( simpleName.getIdentifier() );
			icu.replace(simpleName, ast.newSimpleName(newIdentifier));
			
		}

		// Add changes to the multiple compilation units write object
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
		String icuName = icu.getElementName();
		SimpleName swingWorkerName = (SimpleName) assignment.getLeftHandSide();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.getIdentifier() );
		SWSubscriberModel subscriberDto = createSWSubscriberDto( rxObserverName, icu, refactoringVisitor );

		Map<String, Object> subscriberData = new HashMap<>();
		subscriberData.put( "model", subscriberDto );
		String subscriberTemplate = "subscriber.ftl";

		String subscriberString = TemplateUtils.processTemplate( subscriberTemplate, subscriberData );
		AST ast = assignment.getAST();
		TypeDeclaration typeDeclaration = ASTNodeFactory.createTypeDeclarationFromText( ast, subscriberString );
		
		Statement referenceStatement = ASTNodes.findParent( assignment, Statement.class ).get();
		SwingWorkerASTUtils.addBefore(icu, typeDeclaration, referenceStatement);

		String newAssignmentString = subscriberDto.getSubscriberName() + " = new " + subscriberDto.getClassName() + "()";
		Statement newAssignment = ASTNodeFactory.createSingleStatementFromText( ast, newAssignmentString );
		Statements.addStatementBefore(icu, newAssignment, referenceStatement);
		
		SwingWorkerASTUtils.removeStatement(icu, assignment);

	}

	private void refactorStatelessSwingWorker(
			IRewriteCompilationUnit icu,
			RefactoringVisitor refactoringVisitor,
			Assignment assignment )
	{
		String icuName = icu.getElementName();
		RxObservableModel observableDto = createObservableDto( icu, refactoringVisitor );

		Map<String, Object> observableData = new HashMap<>();
		observableData.put( "model", observableDto );
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );

		AST ast = assignment.getAST();
		Statement observableStatement = ASTNodeFactory.createSingleStatementFromText( ast, observableString );

		Statement referenceStatement = ASTNodes.findParent( assignment, Statement.class ).get();
		Statements.addStatementBefore(icu, observableStatement, referenceStatement);
		
		SimpleName swingWorkerName = (SimpleName) assignment.getLeftHandSide();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.getIdentifier() );
		RxObserverModel observerDto = createObserverDto( rxObserverName, refactoringVisitor, observableDto );
		observerDto.setVariableDecl( false );

		Map<String, Object> observerData = new HashMap<>();
		observerData.put( "model", observerDto );
		String observerTemplate = "observer.ftl";

		String observerString = TemplateUtils.processTemplate( observerTemplate, observerData );
		Statement observerStatement = ASTNodeFactory.createSingleStatementFromText( ast, observerString );
		Statements.addStatementBefore(icu, observerStatement, referenceStatement);

		SwingWorkerASTUtils.removeStatement(icu, assignment);
	}
	}
	
