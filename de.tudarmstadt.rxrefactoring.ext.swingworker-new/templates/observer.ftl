<#if model.observerName??>
    <#if model.variableDecl>SWSubscriber<${model.resultType}, ${model.processType}></#if> ${model.observerName} =
</#if>
    new SWSubscriber<${model.resultType}, ${model.processType}>( ${model.observableName} ) {

<#include "common/processBlock.ftl">

<#include "common/doneBlock.ftl">
};