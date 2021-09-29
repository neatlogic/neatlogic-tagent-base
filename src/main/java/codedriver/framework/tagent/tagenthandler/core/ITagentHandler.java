package codedriver.framework.tagent.tagenthandler.core;

import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ITagentHandler {

    public String getHandler();

    public String getHandlerName();

    public String getName();

    public String execTagentCmd(TagentMessageVo message, TagentVo tagentVo, RunnerVo runnerVo, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
