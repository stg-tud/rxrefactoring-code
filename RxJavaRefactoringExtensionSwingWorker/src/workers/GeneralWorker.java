package workers;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;

import domain.RxObservableDto;
import domain.RxSubscriberDto;
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
		RxObservableDto observableDto = new RxObservableDto( icuName );
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
		observableDto.setDoInBackgroundBlock( refactoringVisitor.getDoInBackgroundBlock().toString() );
		return observableDto;
	}

	protected RxSubscriberDto createObserverDto( String observerName, RefactoringVisitor refactoringVisitor, RxObservableDto observableDto )
	{
		RxSubscriberDto subscriberDto = new RxSubscriberDto();
		subscriberDto.setObserverName( observerName );
		Type resultType = refactoringVisitor.getResultType();
		if ( resultType != null )
		{
			subscriberDto.setResultType( resultType.toString() );
		}
		else
		{
			subscriberDto.setResultType( "Object" );
		}
		Type processType = refactoringVisitor.getProcessType();
		if ( processType != null )
		{
			subscriberDto.setProcessType( processType.toString() );
		}
		else
		{
			subscriberDto.setProcessType( "Object" );
		}
		subscriberDto.setObservableName( observableDto.getVarName() );
		subscriberDto.setChunksName( refactoringVisitor.getProcessVariableName() );
		Block processBlock = refactoringVisitor.getProcessBlock();
		if ( processBlock != null )
		{
			subscriberDto.setProcessBlock( processBlock.toString() );
		}
		Block doneBlock = refactoringVisitor.getDoneBlock();
		if ( doneBlock != null )
		{
			subscriberDto.setDoneBlock( doneBlock.toString() );
		}
		return subscriberDto;
	}

	protected void removeSuperInvocations( RefactoringVisitor refactoringVisitor )
	{
		for ( SuperMethodInvocation methodInvocation : refactoringVisitor.getSuperMethodInvocationsToRemove() )
		{
			Statement statement = ASTUtil.findParent( methodInvocation, Statement.class );
			statement.delete();
		}
	}
}
