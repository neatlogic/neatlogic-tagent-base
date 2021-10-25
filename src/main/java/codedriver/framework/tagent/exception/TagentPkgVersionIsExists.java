package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentPkgVersionIsExists extends ApiRuntimeException {
    public TagentPkgVersionIsExists(String version) {
        super("版本：" + version + " 已存在");
    }
}
