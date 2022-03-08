package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentPkgFormatIllegalException extends ApiRuntimeException {

    private static final long serialVersionUID = -1979153978468669877L;

    public TagentPkgFormatIllegalException(String format) {
        super("tagent安装包格式错误，请上传" + format + "格式的安装包");
    }
}
