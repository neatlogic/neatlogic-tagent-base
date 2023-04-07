package neatlogic.framework.tagent.exception;

import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentActionFailedException extends ApiRuntimeException {

    public TagentActionFailedException(RunnerVo runnerVo, String message) {
        super("exception.tagent.tagentactionfailedexception", runnerVo.getName(), runnerVo.getHost(), message);
    }
}
