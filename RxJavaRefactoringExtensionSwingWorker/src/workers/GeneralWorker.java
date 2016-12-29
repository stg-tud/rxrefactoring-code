package workers;

import org.eclipse.jdt.core.dom.*;

import domain.RxObservableDto;
import domain.RxObserverDto;
import domain.SWSubscriberDto;
import rxjavarefactoring.framework.codegenerators.DynamicIdsMapHolder;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.writers.RxSingleUnitWriter;
import visitors.RefactoringVisitor;
import visitors.RxCollector;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public abstract class GeneralWorker extends AbstractRefactorWorker<RxCollector>
{
	public GeneralWorker( RxCollector rxCollector )
	{
		super( rxCollector );
	}

	protected void updateImports( RxSingleUnitWriter singleUnitWriter )
	{
		singleUnitWriter.addImport( "rx.Observable" );
		singleUnitWriter.addImport( "rx.Emitter" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWEmitter" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWSubscriber" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWDto" );
	}

	protected RxObservableDto createObservableDto( String icuName, RefactoringVisitor refactoringVisitor )
	{
		String varName = "rxObservable" + DynamicIdsMapHolder.getNextObservableId( icuName );
		RxObservableDto observableDto = new RxObservableDto( varName );
		Type resultType = refactoringVisitor.getResultType();
		if ( resultType != null )
		{
			observableDto.setResultType( resultType.toString() );
		}
		else
		{
			observableDto.setResultType( "Object" );
		}
		Type processType = refactoringVisitor.getProcessType();
		if ( processType != null )
		{
			observableDto.setProcessType( processType.toString() );
		}
		else
		{
			observableDto.setProcessType( "Object" );
		}
		observableDto.setDoInBackgroundBlock( removeKeywordThis( refactoringVisitor.getDoInBackgroundBlock().toString() ) );
		return observableDto;
	}

	protected RxObserverDto createObserverDto( String observerName, RefactoringVisitor refactoringVisitor, RxObservableDto observableDto )
	{
		RxObserverDto observerDto = new RxObserverDto();
		observerDto.setObserverName( observerName );
		Type resultType = refactoringVisitor.getResultType();
		if ( resultType != null )
		{
			observerDto.setResultType( resultType.toString() );
		}
		else
		{
			observerDto.setResultType( "Object" );
		}
		Type processType = refactoringVisitor.getProcessType();
		if ( processType != null )
		{
			observerDto.setProcessType( processType.toString() );
		}
		else
		{
			observerDto.setProcessType( "Object" );
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

	protected SWSubscriberDto createSWSubscriberDto( String subscriberName, String icuName, RefactoringVisitor refactoringVisitor )
	{
		String nextObserverId = DynamicIdsMapHolder.getNextObserverId( icuName );
		String className = "RxObserver" + nextObserverId;
		subscriberName = subscriberName + nextObserverId;
		SWSubscriberDto dto = new SWSubscriberDto();
		Type resultType = refactoringVisitor.getResultType();
		if ( resultType != null )
		{
			dto.setResultType( resultType.toString() );
		}
		else
		{
			dto.setResultType( "Object" );
		}
		Type processType = refactoringVisitor.getProcessType();
		if ( processType != null )
		{
			dto.setProcessType( processType.toString() );
		}
		else
		{
			dto.setProcessType( "Object" );
		}
		dto.setClassName( className );
		dto.setSubscriberName( subscriberName );
		dto.setChunksName( refactoringVisitor.getProcessVariableName() );
		Block processBlock = refactoringVisitor.getProcessBlock();
		if ( processBlock != null )
		{
			dto.setProcessBlock( processBlock.toString() );
		}
		Block doneBlock = refactoringVisitor.getDoneBlock();
		if ( doneBlock != null )
		{
			dto.setDoneBlock( doneBlock.toString() );
		}
		dto.setDoInBackgroundBlock( removeKeywordThis( refactoringVisitor.getDoInBackgroundBlock().toString() ) );
		for ( FieldDeclaration fieldDeclaration : refactoringVisitor.getFieldDeclarations() )
		{
			dto.getFieldDeclarations().add( fieldDeclaration.toString() );
		}
		for ( MethodDeclaration methodDeclaration : refactoringVisitor.getAdditionalMethodDeclarations() )
		{
			dto.getMethods().add( methodDeclaration.toString() );
		}
		for ( TypeDeclaration typeDeclaration : refactoringVisitor.getTypeDeclarations() )
		{
			dto.getTypeDeclarations().add( typeDeclaration.toString() );
		}

		return dto;
	}

	private String removeKeywordThis( String block )
	{
		return block.replaceAll( "this\\.", "" );
	}
}
