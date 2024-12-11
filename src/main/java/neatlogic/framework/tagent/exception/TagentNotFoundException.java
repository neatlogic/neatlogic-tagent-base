package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentNotFoundException extends ApiRuntimeException {
    public TagentNotFoundException(String ip, Integer port) {
        super("nfte.tagentnotfoundexception.tagentnotfoundexception.ipport", ip, port.toString());
    }

    public TagentNotFoundException(Long tagentId, String ip, Integer port) {
        super("nfte.tagentnotfoundexception.tagentnotfoundexception.tagentidipport", tagentId, ip, port.toString());
    }
}
