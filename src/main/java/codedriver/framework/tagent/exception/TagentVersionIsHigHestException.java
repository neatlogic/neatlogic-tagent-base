package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentVersionIsHigHestException extends ApiRuntimeException {
    public TagentVersionIsHigHestException(String version) {
        super("当前tagent的版本为：" + version + "，已是最高版本，无需升级");
    }
}
