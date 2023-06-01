package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;
import com.alibaba.fastjson.JSONObject;

/**
 * @author longrf
 * @date 2022/7/13 5:53 下午
 */
public class TagentIpIsEmptyException extends ApiRuntimeException {
    public TagentIpIsEmptyException(JSONObject jsonObject) {

        super("当前注册的tagent的ip为空，注册信息有:{0}", jsonObject.toString());
    }
}
