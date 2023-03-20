package neatlogic.framework.tagent.enums;

import neatlogic.framework.common.constvalue.IEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.util.I18nUtils;

import java.util.List;

public enum TagentAction implements IEnum {
    GET_LOGS("getlogs", "enum.tagent.tagentaction.get_logs"),
    DOWNLOAD_LOG("downloadLog", "enum.tagent.tagentaction.download_log"),
    GET_CONFIG("getConfig", "enum.tagent.tagentaction.get_config"),
    SAVE_CONFIG("saveConfig", "enum.tagent.tagentaction.save_config"),
    RELOAD("reload", "enum.tagent.tagentaction.reload"),
    RESET_CREDENTIAL("resetcred", "enum.tagent.tagentaction.reset_credential"),
    STATUS_CHECK("statusCheck", "enum.tagent.tagentaction.status_check"),
    UPGRADE("upgrade", "enum.tagent.tagentaction.upgrade"),
    BATCH_SAVE_CONFIG("batchSaveConfig", "enum.tagent.tagentaction.batch_save_config")
    ;
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
        return I18nUtils.getMessage(text);
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
