package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentCredUpdateFailedException extends ApiRuntimeException {
    public TagentCredUpdateFailedException(){
        super("tagent密码更新失败");
    }
}
