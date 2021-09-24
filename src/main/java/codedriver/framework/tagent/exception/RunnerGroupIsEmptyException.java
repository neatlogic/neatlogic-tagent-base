package codedriver.framework.tagent.exception;

public class RunnerGroupIsEmptyException extends RuntimeException {
    public RunnerGroupIsEmptyException(Long runnerGroupId) {
        super("runner组id为：" + "的runner组为空，请添加一个新的runner");
    }
}
