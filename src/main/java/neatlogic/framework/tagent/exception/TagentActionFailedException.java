package neatlogic.framework.tagent.exception;

import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentActionFailedException extends ApiRuntimeException {

    public TagentActionFailedException(RunnerVo runnerVo, String message) {
        super("{0} ({1})执行异常，{2}", runnerVo.getName(), runnerVo.getHost(), message);
    }
}
