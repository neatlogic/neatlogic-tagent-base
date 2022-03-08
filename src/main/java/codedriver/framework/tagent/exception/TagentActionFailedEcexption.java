package codedriver.framework.tagent.exception;

import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentActionFailedEcexption extends ApiRuntimeException {

    public TagentActionFailedEcexption(RunnerVo runnerVo, String message) {
        super(runnerVo.getName() + "(" + runnerVo.getHost() + ")执行异常，" + message);
    }
}
