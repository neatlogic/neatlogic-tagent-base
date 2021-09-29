package codedriver.framework.tagent.exception;

public class TagentActionNotFoundEcexption extends RuntimeException {
    public TagentActionNotFoundEcexption(String action) {
        super("没有找到该操作对应的处理方法, 操作类型：" + action);
    }
}
