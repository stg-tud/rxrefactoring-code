private rx.Observable <SWChannel<${model.resultType}, ${model.processType}>> getRxObservable() {
    return rx.Observable.fromEmitter(new SWEmitter <${model.resultType}, ${model.processType}>()
    {
        <#include "common/doInBackgroundBlock.ftl">
    }, Emitter.BackpressureMode.BUFFER );
}