package codedriver.framework.tagent.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class TagentVersionVo {

    @EntityField(name = "os类型", type = ApiParamType.STRING)
    private String osType;
    @EntityField(name = "版本", type = ApiParamType.STRING)
    private String version;
    @EntityField(name = "忽略目录或文件", type = ApiParamType.STRING)
    private String ignoreFile;

    public TagentVersionVo() {

    }

    public TagentVersionVo(String _osType, String _version, String _ignoreFile) {
        this.osType = _osType;
        this.version = _version;
        this.ignoreFile = _ignoreFile;
    }

    public static void main(String[] args) {
        System.out.println(TagentOsType.values());
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIgnoreFile() {
        return ignoreFile;
    }

    public void setIgnoreFile(String ignoreFile) {
        this.ignoreFile = ignoreFile;
    }

    public enum TagentOsType {
        WINDOWS32("win32"), WINDOWS64("win64"), LINUX("linux");
        private String type;

        TagentOsType(String _type) {
            this.type = _type;
        }

        public String getType() {
            return type;
        }
    }
}
