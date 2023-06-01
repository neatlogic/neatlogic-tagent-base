package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentBatchActionCheckLessTagentIpAndPortException extends ApiRuntimeException {
    public TagentBatchActionCheckLessTagentIpAndPortException() {
        super("没有匹配到现有tagent，请正确填写IP或者网段信息");
    }
}
