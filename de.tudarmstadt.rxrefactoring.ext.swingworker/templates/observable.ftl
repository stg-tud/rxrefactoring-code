Observable <SWPackage<${model.resultType}, ${model.processType}>> ${model.varName} =
    Observable.create(new SWEmitter<${model.resultType}, ${model.processType}>() {
    <#include "common/doInBackgroundBlock.ftl">
});