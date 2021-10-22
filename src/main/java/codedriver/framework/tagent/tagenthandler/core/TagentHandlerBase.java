package codedriver.framework.tagent.tagenthandler.core;

import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.exception.RunnerNotFoundException;
import codedriver.framework.tagent.exception.RunnerUnableToAccessEcxeption;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

public abstract class TagentHandlerBase implements ITagentHandler {


    @Override
    public JSONObject execTagentCmd(TagentMessageVo message, TagentVo tagentVo, RunnerVo runnerVo) throws Exception {
        if (runnerVo == null) {
            throw new RunnerNotFoundException(tagentVo.getRunnerId());
        }
        String runnerUrl = runnerVo.getUrl();

        if (StringUtils.isNotBlank(runnerUrl)) {
            if (!runnerUrl.endsWith("/")) {
                runnerUrl = runnerUrl + "/";
            }
        } else {
            throw new RunnerUnableToAccessEcxeption(runnerVo.getId());
        }
        return myExecTagentCmd(message, tagentVo, runnerUrl);
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
