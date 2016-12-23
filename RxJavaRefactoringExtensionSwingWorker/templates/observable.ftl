Observable <SWDto<${dto.resultType}, ${dto.processType}>> ${dto.varName} =
    Observable.fromEmitter(new SWEmitter<${dto.resultType}, ${dto.processType}>() {

    @Override
    protected String doInBackground() throws Exception
    ${dto.doInBackgroundBlock}
}, Emitter.BackpressureMode.BUFFER );