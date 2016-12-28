<#if dto.processBlock??>
    @Override
    protected void process( List<${dto.processType}> ${dto.chunksName} )
    ${dto.processBlock}
</#if>