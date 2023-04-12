package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentPkgVersionIdNotFoundException extends ApiRuntimeException {
    public TagentPkgVersionIdNotFoundException(Long id) {
        super("exception.tagent.tagentpkgversionidnotfoundexception", id);
    }
}
