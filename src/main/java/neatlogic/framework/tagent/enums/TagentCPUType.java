package neatlogic.framework.tagent.enums;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.IEnum;
import neatlogic.framework.util.$;

import java.util.List;

public enum TagentCPUType implements IEnum {
    X86_64("X86_64", "enum.tagent.tagentcputype.X86_64"),
    X86("X86", "enum.tagent.tagentcputype.X86"),
    DEFAULT("default", "default");
    private final String value;
    private final String text;

    TagentCPUType(String value, String text) {
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
        for (TagentCPUType type : values()) {
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
