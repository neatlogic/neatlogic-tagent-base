package codedriver.framework.tagent.exception;

public class RunnerNotFoundInGroupException extends RuntimeException {
    public RunnerNotFoundInGroupException(Long runnerGroupId) {
        super("在id为：" + runnerGroupId + "的runner组里找不到runner");
    }
}
