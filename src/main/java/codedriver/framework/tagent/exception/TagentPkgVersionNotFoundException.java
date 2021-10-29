package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentPkgVersionNotFoundException extends ApiRuntimeException {
    public TagentPkgVersionNotFoundException(String pkgVersion) {
        super("版本为：" + pkgVersion + "的安装包不存在");
    }
}
