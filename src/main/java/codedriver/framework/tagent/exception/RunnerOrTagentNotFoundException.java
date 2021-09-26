package codedriver.framework.tagent.exception;

public class RunnerOrTagentNotFoundException extends RuntimeException {
    public RunnerOrTagentNotFoundException() {
        super("Runner或Tagent返回为空，请检查防火墙配置或Runner,Tagent可用性");
    }
}
