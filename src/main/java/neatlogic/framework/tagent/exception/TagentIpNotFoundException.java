package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.tagent.dto.TagentVo;

public class TagentIpNotFoundException extends ApiRuntimeException {
    public TagentIpNotFoundException(TagentVo tagent) {
        super("exception.tagent.tagentipnotfoundexception", tagent.getName(), tagent.getIp());
    }
}
