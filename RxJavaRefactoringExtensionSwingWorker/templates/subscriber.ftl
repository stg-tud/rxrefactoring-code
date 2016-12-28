class ${dto.className} extends SWSubscriber<${dto.resultType}, ${dto.processType}>{

<#list dto.fieldDeclarations as fieldDeclaration>
    ${fieldDeclaration}
</#list>

${dto.className}() {
    setObservable(getRxObservable());
}

<#include "getRxObservable.ftl">

<#include "common/processBlock.ftl">

<#include "common/doneBlock.ftl">

<#list dto.methods as method>
    ${method}
</#list>

<#list dto.typeDeclarations as typeDeclaration>
    ${typeDeclaration}
</#list>
}