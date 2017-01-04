private rx.Observable <SWDto<${dto.resultType}, ${dto.processType}>> getRxObservable() {
    return rx.Observable.fromEmitter(new SWEmitter <${dto.resultType}, ${dto.processType}>()
    {
        <#include "common/doInBackgroundBlock.ftl">
    }, Emitter.BackpressureMode.BUFFER );
}