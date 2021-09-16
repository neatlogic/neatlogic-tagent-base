package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class RunnerGroupIdNotFoundException extends ApiRuntimeException {
    public RunnerGroupIdNotFoundException(Long id) {
        super("代理组id:" + id + "不存在");
    }
}
