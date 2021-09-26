package codedriver.framework.tagent.tagenthandler.core;

import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.exception.RunnerNotFoundException;
import codedriver.framework.tagent.exception.RunnerNotFoundInGroupException;
import codedriver.framework.tagent.exception.RunnerUnableToAccessEcxeption;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class TagentHandlerBase implements ITagentHandler {


    @Override
    public void execTagentCmd(TagentMessageVo message, TagentVo tagentVo, RunnerVo runnerVo, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
        myExecTagentCmd(message, tagentVo, url, request, response);
    }

    public abstract void myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String url, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
