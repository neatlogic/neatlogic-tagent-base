package codedriver.framework.tagent.tagenthandler.core;

import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import com.alibaba.fastjson.JSONObject;

public interface ITagentHandler {

    public String getHandler();

    public String getHandlerName();

    public String getName();

    public JSONObject execTagentCmd(TagentMessageVo message, TagentVo tagentVo, RunnerVo runnerVo) throws Exception;
}
