package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentPkgVersionIdNotFoundException extends ApiRuntimeException {
    public TagentPkgVersionIdNotFoundException(Long id) {
        super("tagent安装包id：" + id + "不存在");
    }
}
