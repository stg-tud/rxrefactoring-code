package workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

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
		addOrUpdateConstructor(ast, singleUnitWriter, refactoringVisitor, typeDeclaration);

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
		for(MethodInvocation methodInvocation : refactoringVisitor.getAllMethodInvocations()) {
			if(!(refactoringVisitor.getMethodsofsubscriber().get(methodInvocation.getName().getIdentifier()) == null)) {
				ExpressionStatement exprstmnt = (ExpressionStatement)methodInvocation.getParent();
				String methodNameString = className + ".this." + exprstmnt;
				Statement newStatement = ASTNodeFactory.createSingleStatementFromText(ast, methodNameString);
				singleUnitWriter.replaceStatement(exprstmnt, newStatement);
			}
		}
	}

	private void addOrUpdateConstructor(AST ast, RxSingleUnitWriter singleUnitWriter, RefactoringVisitor refactoringVisitor, TypeDeclaration typeDeclaration)
	{
		MethodDeclaration constructor = refactoringVisitor.getConstructor();
		if ( constructor == null )
		{
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
		}
		else
		{
			// add statement to constructor
			Statement setObservableStatement = ASTNodeFactory.createSingleStatementFromText( ast, "setObservable(getRxObservable())" );
			singleUnitWriter.addStatement( setObservableStatement, constructor );
		}
	}

}
