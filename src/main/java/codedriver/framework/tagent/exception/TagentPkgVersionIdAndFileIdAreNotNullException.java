package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentPkgVersionIdAndFileIdAreNotNullException extends ApiRuntimeException {
    public TagentPkgVersionIdAndFileIdAreNotNullException() {
        super("只能选择一个文件创建tagent版本");
    }
}
