package codedriver.framework.tagent.exception;

import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentActionFailedException extends ApiRuntimeException {

    public TagentActionFailedException(RunnerVo runnerVo, String message) {
        super(runnerVo.getName() + "(" + runnerVo.getHost() + ")执行异常，" + message);
    }
}
