package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.tagent.dto.TagentVo;

public class TagentHasBeenConnectedException extends ApiRuntimeException {
    public TagentHasBeenConnectedException(TagentVo vo) {
        super("exception.tagent.tagenthasbeenconnectedexception", vo.getName());
    }
}
