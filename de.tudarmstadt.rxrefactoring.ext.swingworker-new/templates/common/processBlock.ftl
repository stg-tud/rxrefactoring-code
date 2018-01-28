<#if model.processBlock??>
    @Override
    protected void process( List<${model.processType}> ${model.chunksName} )
    ${model.processBlock}
</#if>