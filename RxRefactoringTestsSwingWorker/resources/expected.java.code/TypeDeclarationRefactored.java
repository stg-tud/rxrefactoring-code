package rxrefactoring;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import de.tudarmstadt.stg.rx.swingworker.SWDto;
import de.tudarmstadt.stg.rx.swingworker.SWEmitter;
import de.tudarmstadt.stg.rx.swingworker.SWSubscriber;
import rx.Emitter;

public class TypeDeclaration extends SWSubscriber<String, Integer>
{

	private static final int AMOUNT_OF_WORK = 10;

	private int variable;
	private JLabel resultLabel;
	private JLabel progressLabel;

	public TypeDeclaration( JLabel resultLabel, JLabel progressLabel )
	{
		this.variable = 0;
		this.resultLabel = resultLabel;
		this.progressLabel = progressLabel;
		setObservable( getRxObservable() );
	}

	private rx.Observable<SWDto<String, Integer>> getRxObservable()
	{
		return rx.Observable.fromEmitter( new SWEmitter<String, Integer>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				for ( int i = 0; i < AMOUNT_OF_WORK; i++ )
				{
					publish( i );
					doSomething();
					Thread.sleep( 1000L );
				}
				return "Async Result";
			}
		}, Emitter.BackpressureMode.BUFFER );
	}

	@Override
	protected void process( List<Integer> chunks )
	{
		for ( Integer n : chunks )
		{
			doSomething();
			progressLabel.setText( String.valueOf( n ) );
		}
	}

	@Override
	protected void done()
	{
		try
		{
			String asyncResult = get();
			resultLabel.setText( asyncResult );
		}
		catch ( InterruptedException | ExecutionException e )
		{
			e.printStackTrace();
		}
	}

	private void doSomething()
	{
		variable++;
		System.out.println( variable );
	}

}
