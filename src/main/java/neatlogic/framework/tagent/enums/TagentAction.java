package neatlogic.framework.tagent.enums;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.IEnum;
import neatlogic.framework.util.$;

import java.util.List;

public enum TagentAction implements IEnum {
    GET_LOGS("getlogs", "获取日志列表"),
    DOWNLOAD_LOG("downloadLog", "下载日志"),
    GET_CONFIG("getConfig", "获取配置"),
    SAVE_CONFIG("saveConfig", "保存日志"),
    RELOAD("reload", "重启"),
    RESET_CREDENTIAL("resetcred", "重置密码"),
    STATUS_CHECK("statusCheck", "检查状态"),
    UPGRADE("upgrade", "升级"),
    BATCH_SAVE_CONFIG("batchSaveConfig", "批量修改配置文件")
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
        return $.t(text);
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
