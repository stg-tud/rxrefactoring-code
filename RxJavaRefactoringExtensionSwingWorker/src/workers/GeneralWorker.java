package workers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

import domain.RxObservableModel;
import domain.RxObserverModel;
import domain.SWSubscriberModel;
import rxjavarefactoring.framework.codegenerators.DynamicIdsMapHolder;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
import visitors.RefactoringVisitor;
import visitors.RxCollector;
import writer.RxSwingWorkerWriter;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public abstract class GeneralWorker extends AbstractRefactorWorker<RxCollector>
{

	public static final String KEYWORD_THIS_WITH_DOT = "this.";
	public static final String EMPTY = "";
	public static final String OBJECT_TYPE_NAME = "Object";

	public GeneralWorker( RxCollector rxCollector )
	{
		super( rxCollector );
	}

	protected void updateImports( RxSwingWorkerWriter singleUnitWriter )
	{
		singleUnitWriter.addImport( "rx.Emitter" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWEmitter" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWSubscriber" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWChannel" );
	}

	protected RxObservableModel createObservableDto(String icuName, RefactoringVisitor refactoringVisitor )
	{
		String varName = "rxObservable" + DynamicIdsMapHolder.getNextObservableId( icuName );
		RxObservableModel observableDto = new RxObservableModel( varName );
		Type resultType = refactoringVisitor.getResultType();
		if ( resultType != null )
		{
			observableDto.setResultType( resultType.toString() );
		}
		else
		{
			observableDto.setResultType( OBJECT_TYPE_NAME );
		}
		Type processType = refactoringVisitor.getProcessType();
		if ( processType != null )
		{
			observableDto.setProcessType( processType.toString() );
		}
		else
		{
			observableDto.setProcessType( OBJECT_TYPE_NAME );
		}
		if (refactoringVisitor.getDoInBackgroundBlock() != null) 
		{
			TypeDeclaration typeDeclaration = ASTUtil.findParent( refactoringVisitor.getDoInBackgroundBlock(), TypeDeclaration.class );
			String block = refactoringVisitor.getDoInBackgroundBlock().toString();
			String className = typeDeclaration.getName().getIdentifier();
			observableDto.setDoInBackgroundBlock( specifyClassOfKeywordThis( block, className ) );
		}
		return observableDto;
	}

	protected RxObserverModel createObserverDto(String observerName, RefactoringVisitor refactoringVisitor, RxObservableModel observableDto )
	{
		RxObserverModel observerDto = new RxObserverModel();
		observerDto.setObserverName( observerName );
		Type resultType = refactoringVisitor.getResultType();
		if ( resultType != null )
		{
			observerDto.setResultType( resultType.toString() );
		}
		else
		{
			observerDto.setResultType( OBJECT_TYPE_NAME );
		}
		Type processType = refactoringVisitor.getProcessType();
		if ( processType != null )
		{
			observerDto.setProcessType( processType.toString() );
		}
		else
		{
			observerDto.setProcessType( OBJECT_TYPE_NAME );
		}
		observerDto.setObservableName( observableDto.getVarName() );
		observerDto.setChunksName( refactoringVisitor.getProcessVariableName() );
		Block processBlock = refactoringVisitor.getProcessBlock();
		if ( processBlock != null )
		{
			observerDto.setProcessBlock( processBlock.toString() );
		}
		Block doneBlock = refactoringVisitor.getDoneBlock();
		if ( doneBlock != null )
		{
			observerDto.setDoneBlock( doneBlock.toString() );
		}
		return observerDto;
	}

	protected void removeSuperInvocations( RefactoringVisitor refactoringVisitor )
	{
		for ( SuperMethodInvocation methodInvocation : refactoringVisitor.getSuperMethodInvocationsToRemove() )
		{
			Statement statement = ASTUtil.findParent( methodInvocation, Statement.class );
			statement.delete();
		}
	}

	protected SWSubscriberModel createSWSubscriberDto(String subscriberName, String icuName, RefactoringVisitor refactoringVisitor )
	{
		String nextObserverId = DynamicIdsMapHolder.getNextObserverId( icuName );
		String className = "RxObserver" + nextObserverId;
		subscriberName = subscriberName + nextObserverId;
		SWSubscriberModel model = new SWSubscriberModel();
		Type resultType = refactoringVisitor.getResultType();
		if ( resultType != null )
		{
			model.setResultType( resultType.toString() );
		}
		else
		{
			model.setResultType( OBJECT_TYPE_NAME );
		}
		Type processType = refactoringVisitor.getProcessType();
		if ( processType != null )
		{
			model.setProcessType( processType.toString() );
		}
		else
		{
			model.setProcessType( OBJECT_TYPE_NAME );
		}
		model.setClassName( className );
		model.setSubscriberName( subscriberName );
		model.setChunksName( refactoringVisitor.getProcessVariableName() );
		Block processBlock = refactoringVisitor.getProcessBlock();
		if ( processBlock != null )
		{
			model.setProcessBlock( processBlock.toString() );
		}
		Block doneBlock = refactoringVisitor.getDoneBlock();
		if ( doneBlock != null )
		{
			model.setDoneBlock( doneBlock.toString() );
		}
		String block = refactoringVisitor.getDoInBackgroundBlock().toString();
		model.setDoInBackgroundBlock( specifyClassOfKeywordThis( block, className ) );
		for ( FieldDeclaration fieldDeclaration : refactoringVisitor.getFieldDeclarations() )
		{
			model.getFieldDeclarations().add( fieldDeclaration.toString() );
		}
		for ( MethodDeclaration methodDeclaration : refactoringVisitor.getAdditionalMethodDeclarations() )
		{
			model.getMethods().add( methodDeclaration.toString() );
		}
		for ( TypeDeclaration typeDeclaration : refactoringVisitor.getTypeDeclarations() )
		{
			model.getTypeDeclarations().add( typeDeclaration.toString() );
		}

		return model;
	}

	private String specifyClassOfKeywordThis( String block, String replacement )
	{
		if ( !EMPTY.equals( replacement ) )
		{
			replacement = replacement + "." + KEYWORD_THIS_WITH_DOT;
		}
		StringBuilder sb = new StringBuilder( block );
		List<Integer> indexes = new ArrayList<>();
		int startIndex = 0;
		while ( true )
		{
			int i = block.indexOf( KEYWORD_THIS_WITH_DOT, startIndex );
			if ( i == -1 )
			{
				break;
			}
			indexes.add( i );
			startIndex = i + KEYWORD_THIS_WITH_DOT.length();
		}

		int adjustmentFactor = 0;
		for ( Integer index : indexes )
		{
			int newIndex = index + adjustmentFactor;
			if (index == 0 || (index > 0 && block.charAt(index-1) != '_'
					&& block.charAt(index-1) != '.' && block.charAt(index-1) != '$'
					&& (block.charAt(index-1) < 'a' || block.charAt(index-1) > 'Z' )))
			{
				sb.replace( newIndex, newIndex + KEYWORD_THIS_WITH_DOT.length(), replacement );
				adjustmentFactor += replacement.length() - KEYWORD_THIS_WITH_DOT.length();
			}
		}

		return sb.toString();
	}
}
