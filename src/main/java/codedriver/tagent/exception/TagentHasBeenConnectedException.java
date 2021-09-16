package codedriver.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.tagent.dto.TagentVo;

public class TagentHasBeenConnectedException extends ApiRuntimeException {
    public TagentHasBeenConnectedException(TagentVo vo) {
        super("Tagent:"+vo.getName()+"处于连接状态，请先断开连接");
    }
}
