package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentIdNotFoundException extends ApiRuntimeException {
    public TagentIdNotFoundException(Long id) {
        super("Tagent id:"+id+"不存在");
    }
}
