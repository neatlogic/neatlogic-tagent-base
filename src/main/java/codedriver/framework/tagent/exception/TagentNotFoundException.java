package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentNotFoundException extends ApiRuntimeException {
    public TagentNotFoundException(){
        super("tagent");
    }
}
