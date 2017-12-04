package domain;

/**
 * Description: Model used to fill up the template observer.ftl<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class RxObserverModel
{
	private String observerName;
	private String resultType;
	private String processType;
	private String observableName;
	private String chunksName;
	private String processBlock;
	private String doneBlock;
	private boolean variableDecl;

	public String getObserverName()
	{
		return observerName;
	}

	public void setObserverName( String observerName )
	{
		this.observerName = observerName;
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

	public String getObservableName()
	{
		return observableName;
	}

	public void setObservableName( String observableName )
	{
		this.observableName = observableName;
	}

	public String getChunksName()
	{
		return chunksName;
	}

	public void setChunksName( String chunksName )
	{
		this.chunksName = chunksName;
	}

	public String getProcessBlock()
	{
		return processBlock;
	}

	public void setProcessBlock( String processBlock )
	{
		this.processBlock = processBlock;
	}

	public String getDoneBlock()
	{
		return doneBlock;
	}

	public void setDoneBlock( String doneBlock )
	{
		this.doneBlock = doneBlock;
	}

	public boolean isVariableDecl()
	{
		return variableDecl;
	}

	public void setVariableDecl( boolean variableDecl )
	{
		this.variableDecl = variableDecl;
	}
}
