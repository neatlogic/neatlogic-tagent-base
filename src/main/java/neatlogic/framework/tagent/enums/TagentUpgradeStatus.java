package neatlogic.framework.tagent.enums;

import neatlogic.framework.common.constvalue.IEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.util.I18nUtils;

import java.util.List;

public enum TagentUpgradeStatus implements IEnum {
    SUCCEED("succeed", "enum.tagent.tagentupgradestatus.succeed"),
    FAILED("failed", "enum.tagent.tagentupgradestatus.failed"),
    WORKING("working", "enum.tagent.tagentupgradestatus.working");
    private final String value;
    private final String text;

    TagentUpgradeStatus(String value, String text) {
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
        for (TagentUpgradeStatus type : values()) {
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
