package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.tagent.dto.TagentVo;

public class TagentHasBeenConnectedException extends ApiRuntimeException {
    public TagentHasBeenConnectedException(TagentVo vo) {
        super("tagent({0})处于连接状态，请先断开连接", vo.getName());
    }
}
