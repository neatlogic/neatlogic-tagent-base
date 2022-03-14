package codedriver.framework.tagent.enums;

import codedriver.framework.dependency.core.IFromType;

/**
 * @author longrf
 * @date 2022/3/10 12:11 下午
 */
public enum TagentFromType implements IFromType {
    TAGENT_ACCOUNT("tagentaccount","tagent账号");

    private String value;
    private String text;

    private TagentFromType(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 被调用者类型值
     *
     * @return
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * 被调用者类型名
     *
     * @return
     */
    @Override
    public String getText() {
        return text;
    }
}