package domain;

import rxjavarefactoring.framework.codegenerators.DynamicIdsMapHolder;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class RxSubscriberDto
{
    private String observerName;
    private String resultType;
    private String processType;
    private String observableName;
    private String chunksName;
    private String processBlock;
    private String asyncResultVarName;
    private String doneBlock;
    private String throwableName;
    private String onErrorBlock;

    public RxSubscriberDto(String icuName)
    {
        observerName = "rxObserver" + DynamicIdsMapHolder.getLastObservableId( icuName );
    }

    public String getObserverName()
    {
        return observerName;
    }

    public void setObserverName(String observerName)
    {
        this.observerName = observerName;
    }

    public String getResultType()
    {
        return resultType;
    }

    public void setResultType(String resultType)
    {
        this.resultType = resultType;
    }

    public String getProcessType()
    {
        return processType;
    }

    public void setProcessType(String processType)
    {
        this.processType = processType;
    }

    public String getObservableName()
    {
        return observableName;
    }

    public void setObservableName(String observableName)
    {
        this.observableName = observableName;
    }

    public String getChunksName()
    {
        return chunksName;
    }

    public void setChunksName(String chunksName)
    {
        this.chunksName = chunksName;
    }

    public String getProcessBlock()
    {
        return processBlock;
    }

    public void setProcessBlock(String processBlock)
    {
        this.processBlock = processBlock;
    }

    public String getAsyncResultVarName()
    {
        return asyncResultVarName;
    }

    public void setAsyncResultVarName(String asyncResultVarName)
    {
        this.asyncResultVarName = asyncResultVarName;
    }

    public String getDoneBlock()
    {
        return doneBlock;
    }

    public void setDoneBlock(String doneBlock)
    {
        this.doneBlock = doneBlock;
    }

    public String getThrowableName()
    {
        return throwableName;
    }

    public void setThrowableName(String throwableName)
    {
        this.throwableName = throwableName;
    }

    public String getOnErrorBlock()
    {
        return onErrorBlock;
    }

    public void setOnErrorBlock(String onErrorBlock)
    {
        this.onErrorBlock = onErrorBlock;
    }
}
