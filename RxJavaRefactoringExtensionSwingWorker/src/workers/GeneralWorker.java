package workers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

import domain.RxObservableDto;
import domain.RxObserverDto;
import domain.SWSubscriberDto;
import rxjavarefactoring.framework.codegenerators.DynamicIdsMapHolder;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
import visitors.RefactoringVisitor;
import visitors.RxCollector;
import writer.RxSwingWorkerWriter;

/**
 * Description: <br>
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
//		singleUnitWriter.addImport( "rx.Observable" );
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
			dto.setResultType( OBJECT_TYPE_NAME );
		}
		Type processType = refactoringVisitor.getProcessType();
		if ( processType != null )
		{
			dto.setProcessType( processType.toString() );
		}
		else
		{
			dto.setProcessType( OBJECT_TYPE_NAME );
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
		String block = refactoringVisitor.getDoInBackgroundBlock().toString();
		dto.setDoInBackgroundBlock( specifyClassOfKeywordThis( block, className ) );
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
