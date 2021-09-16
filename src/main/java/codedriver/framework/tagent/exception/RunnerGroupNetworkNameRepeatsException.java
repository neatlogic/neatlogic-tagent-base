package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class RunnerGroupNetworkNameRepeatsException extends ApiRuntimeException {
    public RunnerGroupNetworkNameRepeatsException(String name) {
        super(" 代理组名:" + name + "已经存在");

    }
}
