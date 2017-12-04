package workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

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
 * Created: 12/22/2016
 */
public class FieldDeclarationWorker extends GeneralWorker
{
	public FieldDeclarationWorker( RxCollector rxCollector )
	{
		super( rxCollector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<FieldDeclaration>> fieldDeclMap = collector.getFieldDeclMap();
		int total = fieldDeclMap.values().size();
		monitor.beginTask( getClass().getSimpleName(), total );
		RxLogger.info( this, "METHOD=refactor - Total number of <<FieldDeclaration>>: " + total );

		for ( Map.Entry<ICompilationUnit, List<FieldDeclaration>> fieldDeclEntry : fieldDeclMap.entrySet() )
		{
			ICompilationUnit icu = fieldDeclEntry.getKey();

			for ( FieldDeclaration fieldDeclaration : fieldDeclEntry.getValue() )
			{
				// Get ast and writer
				AST ast = fieldDeclaration.getAST();
				RxSwingWorkerWriter rxSwingWorkerWriter = new RxSwingWorkerWriter(icu, ast, getClass().getSimpleName());
				RxSwingWorkerWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, rxSwingWorkerWriter );

				RxLogger.info( this, "METHOD=refactor - Changing type: " + icu.getElementName() );
				Type type = fieldDeclaration.getType();
				if ( type instanceof ParameterizedType )
				{
					type = ( (ParameterizedType) type ).getType();
				}
				if ( ASTUtil.isClassOf( type, SwingWorkerInfo.getBinaryName() ) )
				{
					singleUnitWriter.replaceType( (SimpleType) type, "SWSubscriber" );
				}

				RxLogger.info( this, "METHOD=refactor - Changing field name: " + icu.getElementName() );
				VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment) fieldDeclaration.fragments().get( 0 );
				String oldIdentifier = varDeclFrag.getName().getIdentifier();
				singleUnitWriter.replaceSimpleName( varDeclFrag.getName(), RefactoringUtils.cleanSwingWorkerName( oldIdentifier ) );

				singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWSubscriber" );

				Expression initializer = varDeclFrag.getInitializer();
				if ( initializer != null && initializer instanceof ClassInstanceCreation )
				{
					ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) initializer;
					if ( ASTUtil.isClassOf( classInstanceCreation, SwingWorkerInfo.getBinaryName() ) )
					{
						RxLogger.info( this, "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
						RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
						classInstanceCreation.accept( refactoringVisitor );

						RxLogger.info( this, "METHOD=refactor - Refactoring assignment in: " + icu.getElementName() );
						refactor( icu, singleUnitWriter, refactoringVisitor, fieldDeclaration );
					}
				}

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
				rxMultipleUnitsWriter.addCompilationUnit( icu );
			}
			monitor.worked( 1 );
		}
		return WorkerStatus.OK;
	}

	private void refactor(
			ICompilationUnit icu,
			RxSwingWorkerWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			FieldDeclaration fieldDeclaration)
	{
		removeSuperInvocations( refactoringVisitor );
		updateImports( singleUnitWriter );

		String icuName = icu.getElementName();
		VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment) fieldDeclaration.fragments().get( 0 );
		String oldIdentifier = varDeclFrag.getName().getIdentifier();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( oldIdentifier );
		SWSubscriberModel subscriberDto = createSWSubscriberDto( rxObserverName, icuName, refactoringVisitor );

		Map<String, Object> subscriberData = new HashMap<>();
		subscriberData.put( "model", subscriberDto );
		String subscriberTemplate = "subscriber.ftl";

		String subscriberString = TemplateUtils.processTemplate( subscriberTemplate, subscriberData );
		AST ast = fieldDeclaration.getAST();
		TypeDeclaration typeDeclaration = ASTNodeFactory.createTypeDeclarationFromText(ast, subscriberString);

		singleUnitWriter.addInnerClassAfter(typeDeclaration, fieldDeclaration);

		ClassInstanceCreation newClassInstanceCreation = ast.newClassInstanceCreation();
		newClassInstanceCreation.setType(ast.newSimpleType(ast.newName(subscriberDto.getClassName())));

		ClassInstanceCreation oldClassInstanceCreation = (ClassInstanceCreation) varDeclFrag.getInitializer();
		singleUnitWriter.replaceNode(newClassInstanceCreation, oldClassInstanceCreation);
	}
}
