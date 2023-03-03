package neatlogic.framework.tagent.tagenthandler.core;

import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.dto.TagentVo;
import com.alibaba.fastjson.JSONObject;

public abstract class TagentHandlerBase implements ITagentHandler {


    @Override
    public JSONObject execTagentCmd(TagentMessageVo message,TagentVo tagentVo,RunnerVo runnerVo) throws Exception {
        return myExecTagentCmd(message, tagentVo, runnerVo);
    }

    /**
     * 执行tagent命令
     * @param message 入参
     * @param tagentVo tagent
     * @param runnerUrl runner url
     * @return 返回信息
     * @throws Exception 异常
     */
    public abstract JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, RunnerVo runnerVo) throws Exception;
}
