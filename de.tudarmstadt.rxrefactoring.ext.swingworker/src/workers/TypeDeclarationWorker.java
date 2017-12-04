package workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
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

import domain.RxObservableModel;
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
import writer.RxSwingWorkerWriter;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/28/2016
 */
public class TypeDeclarationWorker extends GeneralWorker
{
	public TypeDeclarationWorker( RxCollector rxCollector )
	{
		super( rxCollector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<TypeDeclaration>> typeDeclMap = collector.getTypeDeclMap();
		int total = typeDeclMap.values().size();
		monitor.beginTask( getClass().getSimpleName(), total );
		RxLogger.info( this, "METHOD=refactor - Total number of <<TypeDeclaration>>: " + total );

		for ( Map.Entry<ICompilationUnit, List<TypeDeclaration>> typeDeclEntry : typeDeclMap.entrySet() )
		{
			ICompilationUnit icu = typeDeclEntry.getKey();

			for ( TypeDeclaration typeDeclaration : typeDeclEntry.getValue() )
			{
				AST ast = typeDeclaration.getAST();
				RxSwingWorkerWriter rxSwingWorkerWriter = new RxSwingWorkerWriter(icu, ast, getClass().getSimpleName());
				RxSwingWorkerWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, rxSwingWorkerWriter );

				if ( !ASTUtil.isSubclassOf( typeDeclaration, SwingWorkerInfo.getBinaryName(), true ) )
				{
					continue;
				}

				// Collect details about the SwingWorker
				RxLogger.info( this, "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
				RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
				typeDeclaration.accept( refactoringVisitor );

				refactorTypeDeclaration( icu, singleUnitWriter, refactoringVisitor, typeDeclaration );

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
				rxMultipleUnitsWriter.addCompilationUnit( icu );
			}
		}

		return WorkerStatus.OK;
	}

	private void refactorTypeDeclaration(
			ICompilationUnit icu,
			RxSwingWorkerWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			TypeDeclaration typeDeclaration )
	{
		removeSuperInvocations( refactoringVisitor );
		updateImports( singleUnitWriter );
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
		singleUnitWriter.replaceType( (SimpleType) superclassType, "SWSubscriber" );

		AST ast = typeDeclaration.getAST();
		addOrUpdateConstructor(ast, singleUnitWriter, refactoringVisitor, typeDeclaration, resultType);

		Block doInBackgroundBlock = refactoringVisitor.getDoInBackgroundBlock();
		if ( doInBackgroundBlock != null )
		{
			String icuName = icu.getElementName();
			RxObservableModel observableDto = createObservableDto( icuName, refactoringVisitor );
			observableDto.setProcessType( processType );
			observableDto.setResultType( resultType );

			Map<String, Object> observableData = new HashMap<>();
			observableData.put( "model", observableDto );
			String observableTemplate = "getRxObservable.ftl";

			String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );
			MethodDeclaration observableGetMethod = ASTNodeFactory.createMethodFromText( ast, observableString );

			singleUnitWriter.addMethodBefore( observableGetMethod, doInBackgroundBlock );

			MethodDeclaration doInBackgroundDecl = ASTUtil.findParent( doInBackgroundBlock, MethodDeclaration.class );
			singleUnitWriter.removeElement( doInBackgroundDecl );
		}
		methodInvocCheck(ast, singleUnitWriter, refactoringVisitor, typeDeclaration);
	   /*
		* Scenario: 43--kparal--esmska : SMSSender.java
		* Statements like executor.execute(worker) needs to be replaced with observer.executeObservable()
		*/
		removeExecutorServiceStmnt(ast, singleUnitWriter, refactoringVisitor, typeDeclaration);
	}
	
	/**
	 * Check to avoid clashing of method names (with any method presents in RX Java Stream API Classes) 
	 * 
	 * @param ast
	 * @param singleUnitWriter
	 * @param refactoringVisitor
	 * @param typeDeclaration
	 * 
	 */
	private void methodInvocCheck(AST ast, RxSingleUnitWriter singleUnitWriter, RefactoringVisitor refactoringVisitor, TypeDeclaration typeDeclaration) {
		String className = "";
		if(typeDeclaration.getParent() instanceof CompilationUnit) {
			className = typeDeclaration.getName().toString();
		} else if(typeDeclaration.getParent() instanceof TypeDeclaration) {
			className = ((TypeDeclaration)typeDeclaration.getParent()).getName().toString();
		}
		Block block = refactoringVisitor.getDoInBackgroundBlock();
		if(block != null) {
			refactorBlock(block, ast, singleUnitWriter, className, refactoringVisitor, typeDeclaration);
		}
		block = refactoringVisitor.getProcessBlock();
		if(block != null) {
			refactorBlock(block, ast, singleUnitWriter, className, refactoringVisitor, typeDeclaration);
		}
		block = refactoringVisitor.getDoneBlock();
		if(block != null) {
			refactorBlock(block, ast, singleUnitWriter, className, refactoringVisitor, typeDeclaration);
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

	private void refactorBlock(Block block, AST ast, RxSingleUnitWriter singleUnitWriter, String className, RefactoringVisitor refactoringVisitor, TypeDeclaration typeDeclaration) {
		List<Statement> list = block.statements();
		for(int i=0;i<list.size();i++) {
			Statement stmnt = (Statement)list.get(i);
			if(stmnt instanceof ExpressionStatement) {
				refactorExpressionStatement(stmnt, refactoringVisitor, className, ast, singleUnitWriter, typeDeclaration);
			}
		}
	}

	private void refactorExpressionStatement(Statement stmnt, RefactoringVisitor refactoringVisitor, String className, AST ast, RxSingleUnitWriter singleUnitWriter, TypeDeclaration typeDeclaration) {
		ExpressionStatement exprstmnt = (ExpressionStatement)stmnt;
		if(exprstmnt.getExpression() instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) exprstmnt.getExpression();
			if(!(refactoringVisitor.getMethodsofsubscriber().get(methodInvocation.getName().getIdentifier()) == null)) {
				String methodNameString = className + ".this." + exprstmnt;
				Statement newStatement = ASTNodeFactory.createSingleStatementFromText(ast, methodNameString);
				singleUnitWriter.replaceStatement(exprstmnt, newStatement);
			}
		}
	}
	
	/**
	 * remove java.util.concurrent.ExecutorService method call e.g. executor.execute(worker); 
	 * 
	 * @param ast
	 * @param singleUnitWriter
	 * @param refactoringVisitor
	 * @param typeDeclaration
	 * 
	 */
	private void removeExecutorServiceStmnt(AST ast, RxSingleUnitWriter singleUnitWriter, RefactoringVisitor refactoringVisitor, TypeDeclaration typeDeclaration) {
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
											refactorEhncdForStatement(stmnt, refactoringVisitor, ast, singleUnitWriter);
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
	private void refactorEhncdForStatement(Statement stmnt, RefactoringVisitor refactoringVisitor, AST ast, RxSingleUnitWriter singleUnitWriter) {
		List<Statement> enhncdForStmntlist = ((Block)((EnhancedForStatement)stmnt).getBody()).statements();
		for(int j=0;j<enhncdForStmntlist.size();j++) {
			Statement enhncdForstmnt = (Statement)enhncdForStmntlist.get(j);
			if(enhncdForstmnt instanceof ExpressionStatement) {
				ExpressionStatement exprstmnt = (ExpressionStatement)enhncdForstmnt;
				if(exprstmnt.getExpression() instanceof MethodInvocation) {
					MethodInvocation methodInvocation = (MethodInvocation) exprstmnt.getExpression();
					if(exprstmnt != null && exprstmnt.toString().trim().equals("executor.execute(worker);")) {
						Statement newStatement = ASTNodeFactory.createSingleStatementFromText(ast, "rxObserver.executeObservable();");
						singleUnitWriter.replaceStatement(exprstmnt, newStatement);
					}
				}
			}
		}
	}

	private void addOrUpdateConstructor(AST ast, RxSingleUnitWriter singleUnitWriter, RefactoringVisitor refactoringVisitor, TypeDeclaration typeDeclaration, String resultType)
	{
		MethodDeclaration constructor = refactoringVisitor.getConstructor();
		if ( constructor == null)
		{
			if(refactoringVisitor.getDoInBackgroundBlock() != null) {
				// add constructor
				String className = typeDeclaration.getName().toString();
				String constructorString = className + "() { setObservable(getRxObservable()); }";
				MethodDeclaration newConstructor = ASTNodeFactory.createMethodFromText( ast, constructorString );
				newConstructor.setConstructor( true );

				List<MethodDeclaration> allMethodDeclarations = refactoringVisitor.getAllMethodDeclarations();
				if ( !allMethodDeclarations.isEmpty() )
				{
					MethodDeclaration firstMethod = allMethodDeclarations.get( 0 );
					singleUnitWriter.addMethodBefore( newConstructor, firstMethod );
				}
				else
				{
					singleUnitWriter.addMethod( newConstructor, typeDeclaration );
				}
			} else {
				//Abstract class : apply getObservable method in abstract class
				String doInBackgroundBlockStr = "abstract " + resultType + " getRxObservable() throws Exception;";
				MethodDeclaration doInBackgroundBlock = ASTNodeFactory.createMethodFromText( ast, doInBackgroundBlockStr );
				singleUnitWriter.addMethod( doInBackgroundBlock, typeDeclaration);
				
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
													singleUnitWriter.replaceSimpleName(md.getName(), "getRxObservable");	
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
																	singleUnitWriter.replaceSimpleName(mdInner.getName(), "getRxObservable");	
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
				MethodDeclaration doInBackgroundBlock = ASTNodeFactory.createMethodFromText( ast, doInBackgroundBlockStr );
				singleUnitWriter.addMethod( doInBackgroundBlock, typeDeclaration);
			} else {
				// add statement to constructor
				Statement setObservableStatement = ASTNodeFactory.createSingleStatementFromText( ast, "setObservable(getRxObservable())" );
				singleUnitWriter.addStatement( setObservableStatement, constructor );
			}
		}
	}

}
