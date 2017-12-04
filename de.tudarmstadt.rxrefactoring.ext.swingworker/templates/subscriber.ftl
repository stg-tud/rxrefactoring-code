class ${model.className} extends SWSubscriber<${model.resultType}, ${model.processType}>{

<#list model.fieldDeclarations as fieldDeclaration>
    ${fieldDeclaration}
</#list>

${model.className}() {
    setObservable(getRxObservable());
}

<#include "getRxObservable.ftl">

<#include "common/processBlock.ftl">

<#include "common/doneBlock.ftl">

<#list model.methods as method>
    ${method}
</#list>

<#list model.typeDeclarations as typeDeclaration>
    ${typeDeclaration}
</#list>
}