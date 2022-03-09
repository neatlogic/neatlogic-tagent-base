package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentBatchUpgradeCheckLessTagentIpAndPortException extends ApiRuntimeException {
    public TagentBatchUpgradeCheckLessTagentIpAndPortException() {
        super("没有匹配到现有tagent，请正确填写IP或者网段信息");
    }
}
