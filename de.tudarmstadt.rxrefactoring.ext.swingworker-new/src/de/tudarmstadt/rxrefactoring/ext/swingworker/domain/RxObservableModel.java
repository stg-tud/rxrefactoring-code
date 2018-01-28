package de.tudarmstadt.rxrefactoring.ext.swingworker.domain;


/**
 * Description: Model used to fill up the template observable.ftl<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016<br>
 * Adapted to new core by Camila Gonzalez on 18/01/2018
 */
public class RxObservableModel
{
	private String resultType;
	private String processType;
	private String varName;
	private String doInBackgroundBlock;

	/**
	 * The name of the variable if any. Example:<br>
	 * <ul>
	 * <li>rxObservable</li>
	 * <li>rxObservable1</li>
	 * <li>rxObservable2</li>
	 * <li>etc...</li>
	 * </ul>
	 * 
	 * @param varName
	 */
	public RxObservableModel(String varName )
	{
		this.varName = varName;
	}

	public String getResultType()
	{
		return resultType;
	}

	public void setResultType( String resultType )
	{
		this.resultType = resultType;
	}

	public String getProcessType()
	{
		return processType;
	}

	public void setProcessType( String processType )
	{
		this.processType = processType;
	}

	public String getVarName()
	{
		return varName;
	}

	public String getDoInBackgroundBlock()
	{
		return doInBackgroundBlock;
	}

	public void setDoInBackgroundBlock( String doInBackgroundBlock )
	{
		this.doInBackgroundBlock = doInBackgroundBlock;
	}
}
