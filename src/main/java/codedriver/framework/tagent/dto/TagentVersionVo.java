package codedriver.framework.tagent.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BaseEditorVo;
import codedriver.framework.restful.annotation.EntityField;
import codedriver.framework.util.SnowflakeUtil;

public class TagentVersionVo extends BaseEditorVo {

    @EntityField(name = "id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "os类型", type = ApiParamType.STRING)
    private String osType;
    @EntityField(name = "版本", type = ApiParamType.STRING)
    private String version;
    @EntityField(name = "CPU框架", type = ApiParamType.STRING)
    private String osbit;
    @EntityField(name = "忽略目录或文件", type = ApiParamType.STRING)
    private String ignoreFile;
    @EntityField(name = "安装包文件id", type = ApiParamType.LONG)
    private Long fileId;
    @EntityField(name = "安装包文件名", type = ApiParamType.STRING)
    private String fileName;
    @EntityField(name = "附件大小（字节）", type = ApiParamType.INTEGER)
    private Long size;

    public TagentVersionVo() {

    }

    public TagentVersionVo(String _osType, String _version, String osbit) {
        this.osType = _osType;
        this.version = _version;
        this.osbit = osbit;
    }

    public String getOsType() {
        return osType;
    }

    public String getOsbit() {
        return osbit;
    }

    public void setOsbit(String osbit) {
        this.osbit = osbit;
    }

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
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
