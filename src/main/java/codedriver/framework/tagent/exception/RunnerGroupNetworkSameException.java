package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class RunnerGroupNetworkSameException extends ApiRuntimeException {
    public RunnerGroupNetworkSameException(String checkIpMask) {
        super("请删除" + checkIpMask + "的网段");
    }
}
