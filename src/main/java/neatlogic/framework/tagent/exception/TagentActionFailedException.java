package neatlogic.framework.tagent.exception;

import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentActionFailedException extends ApiRuntimeException {

    public TagentActionFailedException(RunnerVo runnerVo, String message) {
        super(runnerVo.getName() + "(" + runnerVo.getHost() + ")执行异常，" + message);
    }
}
