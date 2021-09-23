package codedriver.framework.tagent.exception;

public class RunnerNotFoundException extends RuntimeException {
    public RunnerNotFoundException(Long runnerGroupId) {
        super("在id为：" + runnerGroupId + "的runner组里找不到runner");
    }
}
