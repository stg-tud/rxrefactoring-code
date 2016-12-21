package domain;

import rxjavarefactoring.framework.codegenerators.DynamicIdsMapHolder;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class RxObservableDto
{
	private String resultType;
	private String processType;
	private String varName;
	private String doInBackgroundBlock;

	public RxObservableDto( String icuName )
	{
		varName = "rxObservable" + DynamicIdsMapHolder.getNextObservableId( icuName );
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
