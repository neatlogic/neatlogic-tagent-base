package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class RunnerIdNotFoundException extends ApiRuntimeException {
    public RunnerIdNotFoundException(Long id) {
        super("runner id：" + id + "不存在");
    }
}
