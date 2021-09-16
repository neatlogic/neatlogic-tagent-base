package codedriver.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class RunnerNameRepeatsException extends ApiRuntimeException {
    public RunnerNameRepeatsException(String name) {
        super("已存在名称为：" + name + "的代理");
    }
}
