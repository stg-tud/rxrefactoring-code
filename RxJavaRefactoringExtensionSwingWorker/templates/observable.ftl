Observable <SWDto<${dto.resultType}, ${dto.processType}>> ${dto.varName} =
    Observable.fromEmitter(new SWEmitter<${dto.resultType}, ${dto.processType}>() {

    @Override
    protected ${dto.resultType} doInBackground() throws Exception
    ${dto.doInBackgroundBlock}
}, Emitter.BackpressureMode.BUFFER );