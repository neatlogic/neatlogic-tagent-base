package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class MaskIsIncorrectException extends ApiRuntimeException {
    public MaskIsIncorrectException(String ip) {
        super("ip为：" + ip + "对应的掩码不合法");
    }
}
