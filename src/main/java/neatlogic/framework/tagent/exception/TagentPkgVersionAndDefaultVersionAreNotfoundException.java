package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

import java.io.Serial;

public class TagentPkgVersionAndDefaultVersionAreNotfoundException extends ApiRuntimeException {
    @Serial
    private static final long serialVersionUID = 3066666214496971452L;

    public TagentPkgVersionAndDefaultVersionAreNotfoundException(String pkgVersion) {
        super("nfte.tagentpkgversionanddefaultversionarenotfoundexception.tagentpkgversionanddefaultversionarenotfoundexception", pkgVersion);
    }

}
