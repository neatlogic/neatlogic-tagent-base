package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentPkgVersionIsHighestVersionException extends ApiRuntimeException {
    public TagentPkgVersionIsHighestVersionException(String version) {
        super("当前版本:"+version+"与目标版本相同或更高，无需升级");
    }
}
