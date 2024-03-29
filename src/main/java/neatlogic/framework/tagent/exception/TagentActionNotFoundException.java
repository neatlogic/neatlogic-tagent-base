package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentActionNotFoundException extends ApiRuntimeException {
    public TagentActionNotFoundException(String action) {
        super("没有找到该操作对应的处理方法, 操作类型：{0}", action);
    }
}
