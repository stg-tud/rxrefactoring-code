package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
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
public class VariableDeclStatementWorker extends GeneralWorker
{
	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable RxCollector input,
			@NonNull WorkerSummary summary) throws Exception
	{
		Multimap<IRewriteCompilationUnit, VariableDeclarationStatement> varDeclMap = input.getVarDeclMap();
		int total = varDeclMap.values().size();
		Log.info( getClass(), "METHOD=refactor - Total number of <<VariableDeclarationStatement>>: " + total );

		for (Map.Entry<IRewriteCompilationUnit, VariableDeclarationStatement> varDelcEntry : varDeclMap.entries())
		{
			IRewriteCompilationUnit icu = varDelcEntry.getKey();
			VariableDeclarationStatement varDeclStatement = varDelcEntry.getValue();
			
			AST ast = varDeclStatement.getAST();

			VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDeclStatement.fragments().get( 0 );
			Expression initializer = fragment.getInitializer();
			if ( initializer instanceof ClassInstanceCreation )
			{
				ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) initializer;

				if ( ASTUtils.isClassOf( classInstanceCreation, SwingWorkerInfo.getBinaryName() ) )
				{
					Log.info( getClass(), "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
					RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
					varDeclStatement.accept( refactoringVisitor );

					Log.info( getClass(), "METHOD=refactor - Refactoring variable declaration statement in: " + icu.getElementName() );
					refactorVarDecl( icu, refactoringVisitor, varDeclStatement, fragment );
				}
				else
				{
					Log.info( getClass(), "METHOD=refactor - Refactoring variable name: " + icu.getElementName() );
					SimpleName simpleName = fragment.getName();
					String newIdentifier = RefactoringUtils.cleanSwingWorkerName( simpleName.getIdentifier() );
					icu.replace(simpleName, ast.newSimpleName(newIdentifier));
				}
			} 
			else if (initializer instanceof SimpleName)
			{
	            SimpleName assignedVarSimpleName = (SimpleName) initializer;
		        String newAssignedVarName = RefactoringUtils.cleanSwingWorkerName((assignedVarSimpleName).getIdentifier());
		        icu.replace(assignedVarSimpleName, ast.newSimpleName(newAssignedVarName));
			}

            // change type
            Type type = varDeclStatement.getType();
            if (type instanceof ParameterizedType)
            {
                type = ((ParameterizedType) type).getType();
            }

            if (ASTUtils.isClassOf(type, SwingWorkerInfo.getBinaryName()))
            {
                icu.addImport( "de.tudarmstadt.stg.rx.swingworker.SWSubscriber" );
                icu.replace(type, ast.newSimpleType(ast.newName("SWSubscriber")));
            }

        	String newVarName = RefactoringUtils.cleanSwingWorkerName(fragment.getName().getIdentifier());
       		icu.replace(fragment.getName(), ast.newSimpleName(newVarName));

			// Add changes to the multiple compilation units write object
    		Log.info( getClass(), "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
			
			summary.addCorrect("VariableDeclarations");
		}

		return null;
	}

	private void refactorVarDecl(
			IRewriteCompilationUnit icu,
			RefactoringVisitor refactoringVisitor,
			VariableDeclarationStatement varDeclStatement,
			VariableDeclarationFragment fragment )
	{
		removeSuperInvocations( refactoringVisitor );
		updateImports( icu );

		if ( refactoringVisitor.hasAdditionalFieldsOrMethods() )
		{
			refactorStatefulSwingWorker( icu, refactoringVisitor, varDeclStatement, fragment );
		}
		else
		{
			refactorStatelessSwingWorker( icu, refactoringVisitor, varDeclStatement, fragment );
		}
	}

	private void refactorStatefulSwingWorker(
			IRewriteCompilationUnit icu,
			RefactoringVisitor refactoringVisitor,
			VariableDeclarationStatement varDeclStatement,
			VariableDeclarationFragment fragment )
	{
		String icuName = icu.getElementName();
		SimpleName swingWorkerName = fragment.getName();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.getIdentifier() );
		SWSubscriberModel subscriberDto = createSWSubscriberDto( rxObserverName, icu, refactoringVisitor );

		Map<String, Object> subscriberData = new HashMap<>();
		subscriberData.put( "model", subscriberDto );
		String subscriberTemplate = "subscriber.ftl";

		String subscriberString = TemplateUtils.processTemplate( subscriberTemplate, subscriberData );
		AST ast = varDeclStatement.getAST();
		TypeDeclaration typeDeclaration = ASTNodeFactory.createTypeDeclarationFromText( ast, subscriberString );

		Statements.addStatementBefore(icu, Statements.enclosingStatement(typeDeclaration), varDeclStatement);
		
		String newVarDeclStatementString = "SWSubscriber<" + subscriberDto.getResultType() + ", " + subscriberDto.getProcessType() + "> " +
				subscriberDto.getSubscriberName() + " = new " + subscriberDto.getClassName() + "()";
		Statement newVarDeclStatement = ASTNodeFactory.createSingleStatementFromText( ast, newVarDeclStatementString );
		Statements.addStatementBefore(icu, newVarDeclStatement, varDeclStatement);
		SwingWorkerASTUtils.removeStatement(icu, varDeclStatement);
	}

	private void refactorStatelessSwingWorker(
			IRewriteCompilationUnit icu,
			RefactoringVisitor refactoringVisitor,
			VariableDeclarationStatement varDeclStatement,
			VariableDeclarationFragment fragment )
	{
		String icuName = icu.getElementName();
		RxObservableModel observableDto = createObservableDto( icu, refactoringVisitor );
		/*
		 * Sometimes doInbackground Block needs to be refactored futher for handling java.util.concurrent.ExecutorService.submit() method calls 
		 * executor.submit(singleRun) --> singleRun.executeObservable()
		 * Scenario: CrysHomModeling-master : MainMenu.java
		 */
		observableDto.setDoInBackgroundBlock(refactorDoInBgBlock(refactoringVisitor.getDoInBackgroundBlock()));
		Map<String, Object> observableData = new HashMap<>();
		observableData.put( "model", observableDto );
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );

		AST ast = varDeclStatement.getAST();
		Statement observableStatement = ASTNodeFactory.createSingleStatementFromText( ast, observableString );

		Statements.addStatementBefore(icu, observableStatement, varDeclStatement);
		
		SimpleName swingWorkerName = fragment.getName();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.getIdentifier() );
		RxObserverModel subscriberDto = createObserverDto( rxObserverName, refactoringVisitor, observableDto );
		subscriberDto.setVariableDecl( true );

		Map<String, Object> observerData = new HashMap<>();
		observerData.put( "model", subscriberDto );
		String observerTemplate = "observer.ftl";

		String observerString = TemplateUtils.processTemplate( observerTemplate, observerData );
		Statement observerStatement = ASTNodeFactory.createSingleStatementFromText( ast, observerString );
		Statements.addStatementBefore(icu, observerStatement, varDeclStatement);
		SwingWorkerASTUtils.removeStatement(icu, varDeclStatement);
	}
	
	private String refactorDoInBgBlock(Block doInBgBlock) {
		String doInBgBlockString = "";
		List<Statement> list = doInBgBlock.statements();
		StringBuilder sb = new StringBuilder();
		sb.append("{ " + System.lineSeparator());
		for(int i=0;i<list.size();i++) {
				Statement stmnt = list.get(i);
				if(stmnt instanceof TryStatement && stmnt.toString().contains("executorSC.submit(singleRun)")) {
					stmnt = ASTNodeFactory.createSingleStatementFromText(doInBgBlock.getAST(), stmnt.toString().replace("executorSC.submit(singleRun)", "singleRun.executeObservable()"));
				}
				sb.append(stmnt.toString() + System.lineSeparator());
		}
		sb.append("}");
		doInBgBlockString = RefactoringUtils.cleanSwingWorkerName(sb.toString());
		return doInBgBlockString;
	}

}
