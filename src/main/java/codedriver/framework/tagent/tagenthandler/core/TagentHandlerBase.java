package codedriver.framework.tagent.tagenthandler.core;

import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import com.alibaba.fastjson.JSONObject;

public abstract class TagentHandlerBase implements ITagentHandler {


    @Override
    public JSONObject execTagentCmd(TagentMessageVo message,TagentVo tagentVo,RunnerVo runnerVo) throws Exception {
        return myExecTagentCmd(message, tagentVo, runnerVo.getUrl());
    }

    /**
     * 执行tagent命令
     * @param message 入参
     * @param tagentVo tagent
     * @param runnerUrl runner url
     * @return 返回信息
     * @throws Exception 异常
     */
    public abstract JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String runnerUrl) throws Exception;
}
