package codedriver.tagent.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class TagentMessageVo {

    @EntityField(name = "tagent id", type = ApiParamType.LONG)
    private Long tagentId;
    @EntityField(name = "name", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "data", type = ApiParamType.STRING)
    private String data;
    @EntityField(name = "port", type = ApiParamType.INTEGER)
    private Integer port;
    @EntityField(name = "user", type = ApiParamType.STRING)
    private String user;
    @EntityField(name = "    private String path;\n", type = ApiParamType.STRING)
    private String path;
    @EntityField(name = "runner组id", type = ApiParamType.LONG)
    private Long groupId;
    @EntityField(name = "runner id", type = ApiParamType.LONG)
    private Long runnerId;
    @EntityField(name = "runner ip", type = ApiParamType.STRING)
    private String runnerIp;
    @EntityField(name = "ip", type = ApiParamType.STRING)
    private String ip;

    public Long getTagentId() {
        return tagentId;
    }

    public void setTagentId(Long tagentId) {
        this.tagentId = tagentId;
    }

    public String getType() {
        return name;
    }

    public void setType(String type) {
        this.name = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getRunnerId() {
        return runnerId;
    }

    public void setRunnerId(Long runnerId) {
        this.runnerId = runnerId;
    }

    public String getRunnerIp() {
        return runnerIp;
    }

    public void setRunnerIp(String runnerIp) {
        this.runnerIp = runnerIp;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
