package codedriver.framework.tagent.tagenthandler.core;

import com.alibaba.fastjson.JSONObject;

public abstract class TagentHookBase implements TagentHook {

    @Override
    public final int updateTagentPwd(JSONObject param) throws Exception {
        return myUpdateTagentPwd(param);
    }

    protected abstract int myUpdateTagentPwd(JSONObject param) throws Exception;


}