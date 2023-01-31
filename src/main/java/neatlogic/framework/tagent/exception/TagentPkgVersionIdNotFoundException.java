package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentPkgVersionIdNotFoundException extends ApiRuntimeException {
    public TagentPkgVersionIdNotFoundException(Long id) {
        super("tagent安装包id：" + id + "不存在");
    }
}
