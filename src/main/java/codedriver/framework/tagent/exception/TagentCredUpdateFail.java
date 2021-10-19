package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentCredUpdateFail extends ApiRuntimeException {
    public TagentCredUpdateFail(){
        super("tagent密码更新失败");
    }
}
