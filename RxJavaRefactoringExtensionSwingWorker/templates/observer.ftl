<#if dto.variableDecl>SWSubscriber<${dto.resultType}, ${dto.processType}></#if> ${dto.observerName} =
    new SWSubscriber<${dto.resultType}, ${dto.processType}>( ${dto.observableName} ) {

<#if dto.processBlock??>
    @Override
    protected void process( List<${dto.processType}> ${dto.chunksName} )
    ${dto.processBlock}
</#if>

<#if dto.doneBlock??>
    @Override
    protected void done()
    ${dto.doneBlock}
</#if>
};