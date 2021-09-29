package codedriver.framework.tagent.exception;

public class TagentRunnerConnectRefusedException extends RuntimeException {
    public TagentRunnerConnectRefusedException(String s) {
        super("Runner url： '" + s + "' connect failed");
    }
}
