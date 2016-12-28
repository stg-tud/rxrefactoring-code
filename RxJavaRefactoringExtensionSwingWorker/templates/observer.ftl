<#if dto.observerName??>
    <#if dto.variableDecl>SWSubscriber<${dto.resultType}, ${dto.processType}></#if> ${dto.observerName} =
</#if>
    new SWSubscriber<${dto.resultType}, ${dto.processType}>( ${dto.observableName} ) {

<#include "common/processBlock.ftl">

<#include "common/doneBlock.ftl">
};