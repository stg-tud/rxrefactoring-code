class ${dto.className} extends SWSubscriber<${dto.resultType}, ${dto.processType}>{

<#list dto.fieldDeclarations as fieldDeclaration>
    ${fieldDeclaration}
</#list>

${dto.className}() {
    setObservable(getRxObservable());
}

Observable <SWDto<${dto.resultType}, ${dto.processType}>> getRxObservable() {
    return Observable.fromEmitter(new SWEmitter <${dto.resultType}, ${dto.processType}>()
        {
            @Override
            protected ${dto.resultType} doInBackground() throws Exception
            ${dto.doInBackgroundBlock}
        }, Emitter.BackpressureMode.BUFFER );
}

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

<#list dto.methods as method>
    ${method}
</#list>

<#list dto.typeDeclarations as typeDeclaration>
    ${typeDeclaration}
</#list>
}