package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentActionNotFoundException extends ApiRuntimeException {
    public TagentActionNotFoundException(String action) {
        super("exception.tagent.tagentactionnotfoundexception", action);
    }
}
