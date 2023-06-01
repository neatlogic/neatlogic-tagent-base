package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.tagent.dto.TagentVo;

public class TagentIpNotFoundException extends ApiRuntimeException {
    public TagentIpNotFoundException(TagentVo tagent) {
        super("tagent:“{0}”的IP:“{1}”找不到", tagent.getName(), tagent.getIp());
    }
}
