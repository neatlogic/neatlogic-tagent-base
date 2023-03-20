package neatlogic.framework.tagent.enums;

import neatlogic.framework.dependency.core.IFromType;
import neatlogic.framework.util.I18nUtils;

/**
 * @author longrf
 * @date 2022/3/10 12:11 下午
 */
public enum TagentFromType implements IFromType {
    TAGENT_ACCOUNT("tagentaccount","enum.tagent.tagentfromtype.tagent_account");

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
        return I18nUtils.getMessage(text);
    }
}
