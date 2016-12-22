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

<#if dto.onErrorBlockEnabled>
@Override
public void onError( Throwable ${dto.throwableName} )
{
    <#if dto.timeoutExceptionBlock?has_content && dto.interruptedExceptionBlock?has_content &&
    dto.timeoutExceptionBlock==dto.interruptedExceptionBlock>
    if (${dto.throwableName} instanceof TimeoutException ||
    ${dto.throwableName} instanceof InterruptedException) {
    ${dto.timeoutExceptionBlock}
    return;
    }
    <#else>
        <#if dto.timeoutExceptionBlock??>
        if (${dto.throwableName} instanceof TimeoutException) {
        ${dto.timeoutExceptionBlock}
        return;
        }
        </#if>
        <#if dto.interruptedExceptionBlock??>
        if (${dto.throwableName} instanceof InterruptedException) {
        ${dto.interruptedExceptionBlock}
        return;
        }
        </#if>
    </#if>
Exceptions.propagate( ${dto.throwableName} );
}
</#if>
};