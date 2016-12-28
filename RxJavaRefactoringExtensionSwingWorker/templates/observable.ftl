Observable <SWDto<${dto.resultType}, ${dto.processType}>> ${dto.varName} =
    Observable.fromEmitter(new SWEmitter<${dto.resultType}, ${dto.processType}>() {
    <#include "common/doInBackgroundBlock.ftl">
}, Emitter.BackpressureMode.BUFFER );