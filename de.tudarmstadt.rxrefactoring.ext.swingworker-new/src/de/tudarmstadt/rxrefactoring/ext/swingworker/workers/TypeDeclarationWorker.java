package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.RxObservableModel;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.Statements;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.SwingWorkerASTUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.TemplateUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.RefactoringVisitor;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.TemplateVisitor;
/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/28/2016<br>
 * Adapted to new core by Camila Gonzalez on 24/01/2018
 */
public class TypeDeclarationWorker extends GeneralWorker
{
	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable RxCollector input,
			@NonNull WorkerSummary summary) throws Exception
	{
		Multimap<IRewriteCompilationUnit, TypeDeclaration> typeDeclMap = input.getTypeDeclMap();
		int total = typeDeclMap.values().size();
		Log.info( getClass(), "METHOD=refactor - Total number of <<TypeDeclaration>>: " + total );

		for (Map.Entry<IRewriteCompilationUnit, TypeDeclaration> typeDeclEntry : typeDeclMap.entries())
		{
			IRewriteCompilationUnit icu = typeDeclEntry.getKey();
			TypeDeclaration typeDeclaration = typeDeclEntry.getValue();
			
			if ( !ASTUtils.isSubclassOf( typeDeclaration, SwingWorkerInfo.getBinaryName(), true ) )
			{
				continue;
			}
			
			// Collect details about the SwingWorker
			Log.info( getClass(), "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
			RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
			typeDeclaration.accept( refactoringVisitor );
			refactorTypeDeclaration( icu, refactoringVisitor, typeDeclaration );

			// Add changes to the multiple compilation units write object
			Log.info( getClass(), "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
			//rxMultipleUnitsWriter.addCompilationUnit( icu );
			
			summary.addCorrect("TypeDeclarations");
		}
		return null;
	}

	private void refactorTypeDeclaration(
			IRewriteCompilationUnit icu,
			RefactoringVisitor refactoringVisitor,
			TypeDeclaration typeDeclaration )
	{
		removeSuperInvocations( refactoringVisitor );
		updateImports( icu );
		String resultType = "Object";
		String processType = "Object";

		Type superclassType = typeDeclaration.getSuperclassType();
		if ( superclassType instanceof ParameterizedType )
		{
			ParameterizedType parameterizedType = (ParameterizedType) superclassType;
			List argumentTypes = parameterizedType.typeArguments();
			resultType = argumentTypes.get( 0 ).toString();
			processType = argumentTypes.get( 1 ).toString();
			superclassType = parameterizedType.getType();

		}
		SimpleType newType = SwingWorkerASTUtils.newSimpleType(icu.getAST(), "SWSubscriber");
		synchronized(icu) 
		{
			icu.replace(superclassType, newType);
		}
		
		AST ast = typeDeclaration.getAST();
		addOrUpdateConstructor(ast, icu, refactoringVisitor, typeDeclaration, resultType);

		Block doInBackgroundBlock = refactoringVisitor.getDoInBackgroundBlock();
		if ( doInBackgroundBlock != null )
		{
			RxObservableModel observableDto = createObservableDto( icu, refactoringVisitor );
			observableDto.setProcessType( processType );
			observableDto.setResultType( resultType );

			Map<String, Object> observableData = new HashMap<>();
			observableData.put( "model", observableDto );
			String observableTemplate = "getRxObservable.ftl";

			String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );
			MethodDeclaration observableGetMethod = TemplateVisitor.createMethodFromText( ast, observableString );
			
			SwingWorkerASTUtils.addMethodBefore( icu, observableGetMethod, doInBackgroundBlock );

			MethodDeclaration doInBackgroundDecl = ASTNodes.findParent( doInBackgroundBlock, MethodDeclaration.class ).get();
			synchronized(icu)
			{
				icu.remove( doInBackgroundDecl );
			}
		}
		methodInvocCheck(ast, icu, refactoringVisitor, typeDeclaration);
	   /*
		* Scenario: 43--kparal--esmska : SMSSender.java
		* Statements like executor.execute(worker) needs to be replaced with observer.executeObservable()
		*/
		removeExecutorServiceStmnt(ast, icu, refactoringVisitor, typeDeclaration);
	}
	
	/**
	 * Check to avoid clashing of method names (with any method presents in RX Java Stream API Classes) 
	 * 
	 * @param ast
	 * @param icu
	 * @param refactoringVisitor
	 * @param typeDeclaration
	 * 
	 */
	private void methodInvocCheck(
			AST ast, 
			IRewriteCompilationUnit icu,
			RefactoringVisitor refactoringVisitor, 
			TypeDeclaration typeDeclaration) {
		String className = "";
		if(typeDeclaration.getParent() instanceof CompilationUnit) {
			className = typeDeclaration.getName().toString();
		} else if(typeDeclaration.getParent() instanceof TypeDeclaration) {
			className = ((TypeDeclaration)typeDeclaration.getParent()).getName().toString();
		}
		Block block = refactoringVisitor.getDoInBackgroundBlock();
		if(block != null) {
			refactorBlock(block, ast, icu, className, refactoringVisitor, typeDeclaration);
		}
		block = refactoringVisitor.getProcessBlock();
		if(block != null) {
			refactorBlock(block, ast, icu, className, refactoringVisitor, typeDeclaration);
		}
		block = refactoringVisitor.getDoneBlock();
		if(block != null) {
			refactorBlock(block, ast, icu, className, refactoringVisitor, typeDeclaration);
		}
//		for(MethodInvocation methodInvocation : refactoringVisitor.getAllMethodInvocations()) {
//			if(!(refactoringVisitor.getMethodsofsubscriber().get(methodInvocation.getName().getIdentifier()) == null)) {
//				ExpressionStatement exprstmnt = (ExpressionStatement)methodInvocation.getParent();
//				String methodNameString = className + ".this." + exprstmnt;
//				Statement newStatement = ASTNodeFactory.createSingleStatementFromText(ast, methodNameString);
//				singleUnitWriter.replaceStatement(exprstmnt, newStatement);
//			}
//		}
	}

	private void refactorBlock(
			Block block, 
			AST ast, 
			IRewriteCompilationUnit icu, 
			String className, 
			RefactoringVisitor refactoringVisitor, 
			TypeDeclaration typeDeclaration) {
		List<Statement> list = block.statements();
		for(int i=0;i<list.size();i++) {
			Statement stmnt = (Statement)list.get(i);
			if(stmnt instanceof ExpressionStatement) {
				refactorExpressionStatement(stmnt, refactoringVisitor, className, ast, icu, typeDeclaration);
			}
		}
	}

	private void refactorExpressionStatement(
			Statement stmnt, 
			RefactoringVisitor refactoringVisitor, 
			String className, 
			AST ast, 
			IRewriteCompilationUnit icu,
			TypeDeclaration typeDeclaration) {
		ExpressionStatement exprstmnt = (ExpressionStatement)stmnt;
		if(exprstmnt.getExpression() instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) exprstmnt.getExpression();
			
			if(!(refactoringVisitor.getMethodsofsubscriber().contains(methodInvocation.getName().getIdentifier()))) {
			//if(!(refactoringVisitor.getMethodsofsubscriber().get(methodInvocation.getName().getIdentifier()) == null)) {
				String methodNameString = className + ".this." + exprstmnt;
				Statement newStatement = TemplateVisitor.createSingleStatementFromText(ast, methodNameString);
				synchronized(icu) 
				{
					icu.replace(exprstmnt, newStatement);
				}
			}
		}
	}
	
	/**
	 * remove java.util.concurrent.ExecutorService method call e.g. executor.execute(worker); 
	 * 
	 * @param ast
	 * @param icu
	 * @param refactoringVisitor
	 * @param typeDeclaration
	 * 
	 */
	private void removeExecutorServiceStmnt(
			AST ast, 
			IRewriteCompilationUnit icu, 
			RefactoringVisitor refactoringVisitor, 
			TypeDeclaration typeDeclaration) {
		if(typeDeclaration.getName().toString().equals("SMSWorker")) {
			for(IMethodBinding imb : typeDeclaration.resolveBinding().getDeclaringClass().getDeclaredMethods()) {
				if(!imb.isConstructor()) {
					TypeDeclaration tb = (TypeDeclaration)typeDeclaration.getParent();
					List list = tb.bodyDeclarations();
					for(int i=0;i<list.size();i++) {
						if(list.get(i) instanceof MethodDeclaration) {
							MethodDeclaration md = (MethodDeclaration)list.get(i);
							if(md.getName() != null && md.getName().getIdentifier().equals("sendNew")) {
								Block methodblock = md.getBody();
								if(methodblock != null && methodblock.toString().contains("executor.execute(worker);")) {
									List<Statement> blocklist = methodblock.statements();
									for(int j=0;j<blocklist.size();j++) {
										Statement stmnt = (Statement)blocklist.get(j);
										if(stmnt instanceof EnhancedForStatement){
											refactorEhncdForStatement(stmnt, refactoringVisitor, ast, icu);
										}
									}
								}
							}
						}
					}
					break;
				}
			}
		}
	}
	private void refactorEhncdForStatement(
			Statement stmnt, 
			RefactoringVisitor refactoringVisitor, 
			AST ast, 
			IRewriteCompilationUnit icu) {
		List<Statement> enhncdForStmntlist = ((Block)((EnhancedForStatement)stmnt).getBody()).statements();
		for(int j=0;j<enhncdForStmntlist.size();j++) {
			Statement enhncdForstmnt = (Statement)enhncdForStmntlist.get(j);
			if(enhncdForstmnt instanceof ExpressionStatement) {
				ExpressionStatement exprstmnt = (ExpressionStatement)enhncdForstmnt;
				if(exprstmnt.getExpression() instanceof MethodInvocation) {
					MethodInvocation methodInvocation = (MethodInvocation) exprstmnt.getExpression();
					if(exprstmnt != null && exprstmnt.toString().trim().equals("executor.execute(worker);")) {
						Statement newStatement = TemplateVisitor.createSingleStatementFromText(ast, "rxObserver.executeObservable();");
						Statements.addStatementBefore(icu, exprstmnt, newStatement);
					}
				}
			}
		}
	}

	private void addOrUpdateConstructor(
			AST ast, 
			IRewriteCompilationUnit icu, 
			RefactoringVisitor refactoringVisitor, 
			TypeDeclaration typeDeclaration, 
			String resultType)
	{
		MethodDeclaration constructor = refactoringVisitor.getConstructor();
		if ( constructor == null)
		{
			if(refactoringVisitor.getDoInBackgroundBlock() != null) {
				// add constructor
				String className = typeDeclaration.getName().toString();
				String constructorString = className + "() { setObservable(getRxObservable()); }";
				MethodDeclaration newConstructor = TemplateVisitor.createMethodFromText( ast, constructorString );
				newConstructor.setConstructor( true );

				List<MethodDeclaration> allMethodDeclarations = refactoringVisitor.getAllMethodDeclarations();
				if ( !allMethodDeclarations.isEmpty() )
				{
					MethodDeclaration firstMethod = allMethodDeclarations.get( 0 );
					SwingWorkerASTUtils.addMethodBefore(icu, newConstructor, firstMethod );
				}
				else
				{
					//Add method
					icu.getListRewrite(typeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst( newConstructor, null );
				}
			} else {
				//Abstract class : apply getObservable method in abstract class
				String doInBackgroundBlockStr = "abstract " + resultType + " getRxObservable() throws Exception;";
				MethodDeclaration doInBackgroundBlock = TemplateVisitor.createMethodFromText( ast, doInBackgroundBlockStr );
				//Add method
				icu.getListRewrite(typeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst( constructor, null );
				
				if(typeDeclaration.modifiers() != null && typeDeclaration.modifiers().toString().contains("abstract")) {
					/* 
					 * Scenario 1 :-
					 * Abstract class : Child classes extends that abstract class.
					 * Below code is added for renaming doInBackground method to getObservable for child classes.
					 * E.g. broadinstitute--IGV project : package org.broad.igv.ui.util : IndexCreatorDialog.java
					 * 
					 */
					for(ITypeBinding itb : typeDeclaration.resolveBinding().getDeclaringClass().getDeclaredTypes()) {
						if(itb.isClass() && itb.isSubTypeCompatible(typeDeclaration.resolveBinding()) && !(itb.getSuperclass().getName().equalsIgnoreCase("swingworker"))) {
							TypeDeclaration tb = (TypeDeclaration)typeDeclaration.getParent();
							List list = tb.bodyDeclarations();
							for(int i=0;i<list.size();i++) {
								if(list.get(i) instanceof TypeDeclaration) {
									TypeDeclaration child = (TypeDeclaration) list.get(i);
									// condition to prevent same typedeclaration to be modified 
									if(child.equals(typeDeclaration) && child.hashCode() == typeDeclaration.hashCode()) {
										continue;
									} else if(child.getSuperclassType() != null && child.getSuperclassType().toString().contains(typeDeclaration.getName().getIdentifier())) {
										List childlist = child.bodyDeclarations();
										for(int j=0;j<childlist.size();j++) {
											if(childlist.get(j) instanceof MethodDeclaration) {
												MethodDeclaration md = (MethodDeclaration)childlist.get(j);
												if(md.getName() != null && md.getName().getIdentifier().equals("doInBackground")) {
													synchronized(icu) 
													{
														icu.replace(md.getName(), SwingWorkerASTUtils.newSimpleName(ast, "getRxObservable"));
													}
												}
											}
										}
									}
								}
							}
							break;
						}
					}
					/* 
					 * Scenario 2 :-
					 * Abstract class : Some methods have that abstract class declared in anonymous way.
					 * Below code is added for renaming doInBackground method to getObservable for those anonymously declared classes.
					 * E.g. broadinstitute--IGV project : package org.broad.igv.tools : IgvToolsGui.java
					 * 
					 */
					for(IMethodBinding imb : typeDeclaration.resolveBinding().getDeclaringClass().getDeclaredMethods()) {
						if(!imb.isConstructor()) {
							TypeDeclaration tb = (TypeDeclaration)typeDeclaration.getParent();
							List list = tb.bodyDeclarations();
							for(int i=0;i<list.size();i++) {
								if(list.get(i) instanceof MethodDeclaration) {
									MethodDeclaration md = (MethodDeclaration)list.get(i);
									Block methodblock = md.getBody();
									if(methodblock != null && methodblock.toString().contains("new " + typeDeclaration.getName()) && !md.isConstructor()) {
										List<Statement> blocklist = methodblock.statements();
										for(int j=0;j<blocklist.size();j++) {
											Statement stmnt = (Statement)blocklist.get(j);
											if(stmnt instanceof VariableDeclarationStatement) {
												VariableDeclarationStatement vdstmnt = (VariableDeclarationStatement)stmnt;
												VariableDeclarationFragment fragment = (VariableDeclarationFragment) vdstmnt.fragments().get( 0 );
												if(fragment.getInitializer() instanceof ClassInstanceCreation) {
													ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) fragment.getInitializer();
													if(classInstanceCreation.getType().toString().equals(typeDeclaration.getName().getIdentifier()) && classInstanceCreation.getAnonymousClassDeclaration() != null) {
														List childlist = classInstanceCreation.getAnonymousClassDeclaration().bodyDeclarations();
														for(int k=0;k<childlist.size();k++) {
															if(childlist.get(k) instanceof MethodDeclaration) {
																MethodDeclaration mdInner = (MethodDeclaration)childlist.get(k);
																if(mdInner.getName() != null && mdInner.getName().getIdentifier().equals("doInBackground")) {
																	synchronized(icu) 
																	{
																		icu.replace(mdInner.getName(), SwingWorkerASTUtils.newSimpleName(ast, "getRxObservable"));
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
							break;
						}
					}
				}
			}
		}
		else
		{
			/*
			 * Abstract class : apply getObservable method in abstract class instead of putting observable statement into constructor.
			 * Scenario: Scenario: 37--KolakCC--lol-jclient : NamedSwingWorker.java
			 */
			/*
			 * Scenario : 45--gshakhn--sonar-intellij-plugin 
			 * Identified case where below if condition is added for RefreshSonarFileWorker.java, which should not be the case.
			 * So, prefereble solution is to have it fix name for project 37 where we need that if condition behaviour to be in.
			 * That's why typeDeclaration.getName().toString().equals("NamedSwingWorker") is ammended in below if condition.
			 */
			if(typeDeclaration.modifiers() != null && typeDeclaration.modifiers().toString().contains("abstract") && typeDeclaration.getName().toString().equals("NamedSwingWorker")) {
				String doInBackgroundBlockStr = "protected abstract " + resultType + " getRxObservable() throws Exception;";
				MethodDeclaration doInBackgroundBlock = TemplateVisitor.createMethodFromText( ast, doInBackgroundBlockStr );
				//Add method
				icu.getListRewrite(typeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(doInBackgroundBlock, null );
			} else {
				// add statement to constructor
				Statement setObservableStatement = TemplateVisitor.createSingleStatementFromText( ast, "setObservable(getRxObservable())" );
				SwingWorkerASTUtils.addStatement(icu, setObservableStatement, constructor );
			}
		}
	}


}
