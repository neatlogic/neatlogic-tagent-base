package codedriver.framework.tagent.exception;

public class RunnerNotFoundException extends RuntimeException{
    public RunnerNotFoundException(Long runnerId) {
        super("runner id 为："+runnerId+"找不到");
    }
}
