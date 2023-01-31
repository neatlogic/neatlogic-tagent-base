package neatlogic.framework.tagent.dto;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BaseEditorVo;
import neatlogic.framework.restful.annotation.EntityField;
import neatlogic.framework.util.SnowflakeUtil;

public class TagentUpgradeAuditVo extends BaseEditorVo {
    private static final long serialVersionUID = -6989098403057900024L;
    @EntityField(name = "id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "记录详细表 id", type = ApiParamType.LONG)
    private Long auditId;
    @EntityField(name = "升级个数", type = ApiParamType.INTEGER)
    private Integer count;
    @EntityField(name = "网段", type = ApiParamType.STRING)
    private String network;
    @EntityField(name = "ip", type = ApiParamType.STRING)
    private String ip;
    @EntityField(name = "端口", type = ApiParamType.INTEGER)
    private Integer port;
    @EntityField(name = "原版本", type = ApiParamType.STRING)
    private String sourceVersion;
    @EntityField(name = "目标版本", type = ApiParamType.STRING)
    private String targetVersion;
    @EntityField(name = "runner id", type = ApiParamType.LONG)
    private Long runnerId;
    @EntityField(name = "runner name", type = ApiParamType.STRING)
    private String runnerName;
    @EntityField(name = "状态（升级是否成功）", type = ApiParamType.STRING)
    private String status;
    @EntityField(name = "结果", type = ApiParamType.STRING)
    private String result;

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }

    public TagentUpgradeAuditVo(Long auditId, String ip, Integer port, String sourceVersion, String targetVersion, String status) {
        this.auditId = auditId;
        this.ip = ip;
        this.port = port;
        this.sourceVersion = sourceVersion;
        this.targetVersion = targetVersion;
        this.status = status;

    }

    public TagentUpgradeAuditVo() {

    }


    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getSourceVersion() {
        return sourceVersion;
    }

    public void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    public String getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }

    public Long getRunnerId() {
        return runnerId;
    }

    public void setRunnerId(Long runnerId) {
        this.runnerId = runnerId;
    }

    public String getRunnerName() {
        return runnerName;
    }

    public void setRunnerName(String runnerName) {
        this.runnerName = runnerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
