private rx.Observable <SWPackage<${model.resultType}, ${model.processType}>> getRxObservable() {
    return rx.Observable.create(new SWEmitter <${model.resultType}, ${model.processType}>()
    {
        <#include "common/doInBackgroundBlock.ftl">
    }, Emitter.BackpressureMode.BUFFER );
}