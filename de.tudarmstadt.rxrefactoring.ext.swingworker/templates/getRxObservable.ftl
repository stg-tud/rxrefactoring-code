private Observable <SWPackage<${model.resultType}, ${model.processType}>> getRxObservable() {
    return Observable.create(new SWEmitter <${model.resultType}, ${model.processType}>()
    {
        <#include "common/doInBackgroundBlock.ftl">
    });
}