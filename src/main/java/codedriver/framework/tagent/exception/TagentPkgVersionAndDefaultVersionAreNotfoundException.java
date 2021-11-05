package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentPkgVersionAndDefaultVersionAreNotfoundException extends ApiRuntimeException {
    public TagentPkgVersionAndDefaultVersionAreNotfoundException(String pkgVersion) {
        super("版本为：" + pkgVersion + "的安装包和对应版本的默认安装包都不存在，且现在已是最高版本");
    }

}
