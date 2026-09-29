package neatlogic.framework.tagent.enums;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.IEnum;
import neatlogic.framework.util.I18n;

import java.util.List;

public enum TagentStatus implements IEnum {
    CONNECTED("connected", new I18n("common.connected")),
    DISCONNECTED("disconnected", new I18n("common.disconnected"));
    private final String value;
    private final I18n text;

    TagentStatus(String value, I18n text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text.toString();
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
