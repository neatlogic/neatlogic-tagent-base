package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class IPIsIncorrectException extends ApiRuntimeException {
    public IPIsIncorrectException(String ip) {
        super("ip:" + ip + "不合法");
    }
}
