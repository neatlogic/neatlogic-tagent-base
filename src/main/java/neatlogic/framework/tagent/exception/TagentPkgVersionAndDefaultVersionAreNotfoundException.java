package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentPkgVersionAndDefaultVersionAreNotfoundException extends ApiRuntimeException {
    public TagentPkgVersionAndDefaultVersionAreNotfoundException(String pkgVersion) {
        super("exception.tagent.tagentpkgversionanddefaultversionarenotfoundexception", pkgVersion);
    }

}
