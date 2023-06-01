package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentPkgVersionIdNotFoundException extends ApiRuntimeException {
    public TagentPkgVersionIdNotFoundException(Long id) {
        super("tagent安装包id：{0}不存在", id);
    }
}
