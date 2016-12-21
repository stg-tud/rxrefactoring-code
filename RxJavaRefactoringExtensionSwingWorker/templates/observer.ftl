${dto.observerName} = new SWSubscriber<${dto.resultType}, ${dto.processType}>( ${dto.observableName} ) {

<#if dto.processBlock??>
    @Override
    protected void process( List<${dto.processType}> ${dto.chunksName} )
    ${dto.processBlock}
</#if>

<#if dto.doneBlock??>
    @Override
    protected void done( String ${dto.asyncResultVarName} )
    ${dto.doneBlock}
</#if>

<#if dto.onErrorBlock??>
    @Override
    public void onError( Throwable ${dto.throwableName} )
    ${dto.onErrorBlock}
</#if>
};