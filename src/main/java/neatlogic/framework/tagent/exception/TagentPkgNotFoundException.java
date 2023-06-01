package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentPkgNotFoundException extends ApiRuntimeException {
    public TagentPkgNotFoundException(Long fileId) {
        super("tagent安装包：{0}不存在", fileId);
    }

    public TagentPkgNotFoundException() {
        super("没有找到安装包文件");
    }
}
