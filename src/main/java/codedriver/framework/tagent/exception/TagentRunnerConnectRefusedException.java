package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentRunnerConnectRefusedException extends ApiRuntimeException {

    public TagentRunnerConnectRefusedException(String url, String message) {
        super("Runner url： '" + url + "' connect failed,runner报错信息：" + message);
    }
}
