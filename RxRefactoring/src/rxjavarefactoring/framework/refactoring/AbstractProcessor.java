package rxjavarefactoring.framework.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;

import rxjavarefactoring.framework.writers.RxMultipleChangeWriter;

/**
 * Description: Abstract processor. It forces defining the minimum requirements
 * for a processor<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public abstract class AbstractProcessor<T extends AbstractCollector> extends Refactoring
{
	private final String name;

	protected final T collector;
	protected final RxMultipleChangeWriter rxMultipleChangeWriter;

	public AbstractProcessor( T collector, String name )
	{
		this.name = name;
		this.collector = collector;
		rxMultipleChangeWriter = new RxMultipleChangeWriter();
	}

	@Override
	public String getName()
	{
		return name;
	}
}
