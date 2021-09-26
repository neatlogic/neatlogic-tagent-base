package codedriver.framework.tagent.enums;

import codedriver.framework.common.constvalue.IEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

public enum TagentAction implements IEnum {
    GETLOGS("getlogs", "获取日志");
    private final String value;
    private final String text;

    TagentAction(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    @Override
    public List getValueTextList() {
        JSONArray array = new JSONArray();
        for (TagentAction action : values()) {
            array.add(new JSONObject() {
                {
                    this.put("value", action.getValue());
                    this.put("text", action.getText());
                }
            });
        }
        return array;
    }
}