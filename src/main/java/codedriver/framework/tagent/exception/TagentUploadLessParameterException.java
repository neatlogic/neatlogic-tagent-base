package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentUploadLessParameterException extends ApiRuntimeException {
    public TagentUploadLessParameterException() {
        super("tagent上传安装包 缺少必要参数");
    }
}
