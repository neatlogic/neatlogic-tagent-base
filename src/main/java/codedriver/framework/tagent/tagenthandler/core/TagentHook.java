package codedriver.framework.tagent.tagenthandler.core;

import com.alibaba.fastjson.JSONObject;

public interface TagentHook {
    int updateTagentPwd(JSONObject param) throws Exception;
}
