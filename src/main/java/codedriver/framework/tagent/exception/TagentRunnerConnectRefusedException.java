package codedriver.framework.tagent.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class TagentRunnerConnectRefusedException extends ApiRuntimeException {

    private static final long serialVersionUID = 1958411996207578533L;

    public TagentRunnerConnectRefusedException(String url, String message) {
        super("Runner url： '" + url + "' " + message);
    }
}
