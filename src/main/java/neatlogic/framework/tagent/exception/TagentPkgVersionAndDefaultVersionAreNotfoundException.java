package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TagentPkgVersionAndDefaultVersionAreNotfoundException extends ApiRuntimeException {
    public TagentPkgVersionAndDefaultVersionAreNotfoundException(String pkgVersion) {
        super("版本为：{0}的安装包和对应版本的默认安装包都不存在,请检查对应的安装包", pkgVersion);
    }

}
