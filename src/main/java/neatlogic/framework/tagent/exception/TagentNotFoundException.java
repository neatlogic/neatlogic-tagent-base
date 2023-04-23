package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentNotFoundException extends ApiRuntimeException {
    public TagentNotFoundException(String ip, Integer port) {
        super("exception.tagent.tagentnotfoundexception", ip, port.toString());
    }
}
