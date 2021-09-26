package codedriver.framework.tagent.exception;

public class RunnerUnableToAccessEcxeption extends RuntimeException {
    public RunnerUnableToAccessEcxeption(Long id) {
        super("无法访问的runner id:" + id + "的runner");

    }
}
