package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentPkgVersionIdAndFileIdAreNullException extends ApiRuntimeException {
    public TagentPkgVersionIdAndFileIdAreNullException() {
        super("必须选择一个文件创建tagent版本");
    }
}
