package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;
import com.alibaba.fastjson.JSONObject;

/**
 * @author longrf
 * @date 2022/7/13 5:53 下午
 */
public class TagentPortIsEmptyException extends ApiRuntimeException {
    public TagentPortIsEmptyException(JSONObject jsonObject) {
        super("exception.tagent.tagentportisemptyexception", jsonObject.toString());
    }
}
