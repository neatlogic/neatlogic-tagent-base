package neatlogic.framework.tagent.enums;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.IEnum;
import neatlogic.framework.util.$;

import java.util.List;

public enum TagentStatus implements IEnum {
    CONNECTED("connected", "已连接"),
    DISCONNECTED("disconnected", "未连接");
    private final String value;
    private final String text;

    TagentStatus(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return $.t(text);
    }

    @Override
    public List getValueTextList() {
        JSONArray array = new JSONArray();
        for (TagentStatus type : values()) {
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
