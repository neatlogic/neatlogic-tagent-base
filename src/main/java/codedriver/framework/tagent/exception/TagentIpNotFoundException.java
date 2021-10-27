package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.tagent.dto.TagentVo;

public class TagentIpNotFoundException extends ApiRuntimeException {
    public TagentIpNotFoundException(TagentVo tagent) {
        super("tagent:" + tagent.getName() + "的IP找不到");
    }
}
