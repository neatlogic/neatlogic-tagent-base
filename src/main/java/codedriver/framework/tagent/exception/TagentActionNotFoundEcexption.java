package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentActionNotFoundEcexption extends ApiRuntimeException {
    public TagentActionNotFoundEcexption(String action) {
        super("没有找到该操作对应的处理方法, 操作类型：" + action);
    }
}
