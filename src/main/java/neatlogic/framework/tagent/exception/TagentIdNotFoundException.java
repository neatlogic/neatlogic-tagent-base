package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentIdNotFoundException extends ApiRuntimeException {
    public TagentIdNotFoundException(Long id) {
        super("tagent id:"+id+"不存在");
    }
}