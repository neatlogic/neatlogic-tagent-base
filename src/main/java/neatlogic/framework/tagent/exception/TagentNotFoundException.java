package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentNotFoundException extends ApiRuntimeException {
    public TagentNotFoundException(String ip, Integer port) {
        super("通过ip:“" + ip + "”和port:“" + port + "”找不到tagent，请重新注册");
    }
}
