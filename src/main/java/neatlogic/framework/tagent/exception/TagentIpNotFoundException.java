package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.tagent.dto.TagentVo;

public class TagentIpNotFoundException extends ApiRuntimeException {
    public TagentIpNotFoundException(TagentVo tagent) {
        super("tagent:“" + tagent.getName() + "”的IP:“" + tagent.getIp() + "”找不到");
    }
}
