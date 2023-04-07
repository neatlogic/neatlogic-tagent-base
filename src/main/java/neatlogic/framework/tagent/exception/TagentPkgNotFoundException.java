package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentPkgNotFoundException extends ApiRuntimeException {
    public TagentPkgNotFoundException(Long fileId) {
        super("exception.tagent.tagentpkgnotfoundexception", fileId);
    }

    public TagentPkgNotFoundException() {
        super("exception.tagent.tagentpkgnotfoundexception.1");
    }
}
