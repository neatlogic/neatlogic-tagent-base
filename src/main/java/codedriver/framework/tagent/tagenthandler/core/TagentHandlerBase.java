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
        String url = runnerVo.getUrl();

        if (StringUtils.isNotBlank(url)) {
            if (!url.endsWith("/")) {
                url = url + "/";
            }
        } else {
            throw new RunnerUnableToAccessEcxeption(runnerVo.getId());
        }
        return myExecTagentCmd(message, tagentVo, url);
    }

    public abstract JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String url) throws Exception;
}
