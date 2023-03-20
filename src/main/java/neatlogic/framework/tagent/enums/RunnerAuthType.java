package neatlogic.framework.tagent.enums;

import neatlogic.framework.common.constvalue.IEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.util.I18nUtils;

import java.util.List;

public enum RunnerAuthType implements IEnum {
    BASIC("basic", "enum.tagent.runnerauthtype.basic"),
    HMAC("hmac", "enum.tagent.runnerauthtype.hmac");
    private final String value;
    private final String text;

    RunnerAuthType(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return I18nUtils.getMessage(text);
    }

    @Override
    public List getValueTextList() {
        JSONArray array = new JSONArray();
        for (RunnerAuthType type : values()) {
            array.add(new JSONObject() {
                {
                    this.put("value", type.getValue());
                    this.put("text", type.getText());
                }
            });
        }
        return array;
    }
}
