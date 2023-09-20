package neatlogic.framework.tagent.tagenthandler.core;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.runner.RunnerNotFoundByTagentRunnerIdException;
import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.dto.TagentVo;

public abstract class TagentHandlerBase implements ITagentHandler {


    @Override
    public JSONObject execTagentCmd(TagentMessageVo message,TagentVo tagentVo,RunnerVo runnerVo) throws Exception {
        if(runnerVo == null){
            throw new RunnerNotFoundByTagentRunnerIdException(message.getRunnerId());
        }
        return myExecTagentCmd(message, tagentVo, runnerVo);
    }

    /**
     * 执行tagent命令
     * @param message 入参
     * @param tagentVo tagent
     * @param runnerVo runner
     * @return 返回信息
     * @throws Exception 异常
     */
    public abstract JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, RunnerVo runnerVo) throws Exception;
}
