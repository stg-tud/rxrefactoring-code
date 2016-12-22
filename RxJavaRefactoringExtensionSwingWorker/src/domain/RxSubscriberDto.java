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
    private boolean onErrorBlockEnabled;
    private String timeoutExceptionBlock;
    private String interruptedExceptionBlock;

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

    public boolean isOnErrorBlockEnabled()
    {
        return onErrorBlockEnabled;
    }

    public void setOnErrorBlockEnabled(boolean onErrorBlockEnabled)
    {
        this.onErrorBlockEnabled = onErrorBlockEnabled;
    }

    public String getTimeoutExceptionBlock()
    {
        return timeoutExceptionBlock;
    }

    public void setTimeoutExceptionBlock(String timeoutExceptionBlock)
    {
        this.timeoutExceptionBlock = timeoutExceptionBlock;
    }

    public String getInterruptedExceptionBlock()
    {
        return interruptedExceptionBlock;
    }

    public void setInterruptedExceptionBlock(String interruptedExceptionBlock)
    {
        this.interruptedExceptionBlock = interruptedExceptionBlock;
    }
}
