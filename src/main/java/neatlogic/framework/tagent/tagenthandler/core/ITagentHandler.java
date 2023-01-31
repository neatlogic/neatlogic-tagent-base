package neatlogic.framework.tagent.tagenthandler.core;

import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.dto.TagentVo;
import com.alibaba.fastjson.JSONObject;

public interface ITagentHandler {

    String getHandler();

    String getHandlerName();

    String getName();

    JSONObject execTagentCmd(TagentMessageVo message, TagentVo tagentVo, RunnerVo runnerVo) throws Exception;
}
