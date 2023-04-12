package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

/**
 * @author longrf
 * @date 2022/4/21 12:02 下午
 */
public class TagentIdIsRepeatException extends ApiRuntimeException {
    public TagentIdIsRepeatException(Long id, String ip, Integer port) {
        super("exception.tagent.tagentidisrepeatexception", id, ip, port);
    }
}
