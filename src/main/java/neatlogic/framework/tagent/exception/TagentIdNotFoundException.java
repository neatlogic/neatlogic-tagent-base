package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentIdNotFoundException extends ApiRuntimeException {
    public TagentIdNotFoundException(Long id) {
        super("exception.tagent.tagentidnotfoundexception", id);
    }
}
