/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.framework.tagent.dto;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BaseEditorVo;
import neatlogic.framework.common.util.RC4Util;
import neatlogic.framework.restful.annotation.EntityField;
import neatlogic.framework.util.SnowflakeUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author lvzk
 * @since 2021/8/23 15:20
 */
public class TagentVo extends BaseEditorVo {
    //不能改，tagent也使用了这个key
    public static final String encryptKey = "#ts=9^0$1";
    private static final long serialVersionUID = -6983563143057900024L;
    @EntityField(name = "主键id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "tagentIP", type = ApiParamType.STRING)
    private String ip;
    @EntityField(name = "ipString", type = ApiParamType.STRING)
    private String ipString;
    @EntityField(name = "ipList", type = ApiParamType.JSONARRAY)
    private List<String> ipList;
    @EntityField(name = "tagent注册端口", type = ApiParamType.INTEGER)
    private Integer port;
    @EntityField(name = "tagent名称", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "tagent版本", type = ApiParamType.STRING)
    private String version;
    @EntityField(name = "osId", type = ApiParamType.STRING)
    private Long osId;
    @EntityField(name = "系统名", type = ApiParamType.STRING)
    private String osName;
    @EntityField(name = "系统名/系统版本", type = ApiParamType.STRING)
    private String osNameVersion;
    @EntityField(name = "系统类型", type = ApiParamType.STRING)
    private String osType;
    @EntityField(name = "系统版本", type = ApiParamType.STRING)
    private String osVersion;
    @EntityField(name = "cpu架构", type = ApiParamType.STRING)
    private String osbit;
    @EntityField(name = "密码", type = ApiParamType.STRING)
    private String credential;
    @EntityField(name = "帐号id", type = ApiParamType.LONG)
    private Long accountId;
    @EntityField(name = "runner id", type = ApiParamType.LONG)
    private Long runnerId;
    @EntityField(name = "runner name", type = ApiParamType.STRING)
    private String runnerName;
    @EntityField(name = "runner组id", type = ApiParamType.LONG)
    private Long runnerGroupId;
    @EntityField(name = "runner组name", type = ApiParamType.STRING)
    private String runnerGroupName;
    @EntityField(name = "runner ip", type = ApiParamType.STRING)
    private String runnerIp;
    @EntityField(name = "runner port", type = ApiParamType.STRING)
    private String runnerPort;
    @EntityField(name = "tagent用户", type = ApiParamType.STRING)
    private String user;
    @EntityField(name = "cpu占用", type = ApiParamType.STRING)
    private String pcpu;
    @EntityField(name = "内存占用", type = ApiParamType.STRING)
    private String mem;
    @EntityField(name = "连接失败原因", type = ApiParamType.STRING)
    private String disConnectReason;
    @EntityField(name = "tagent状态", type = ApiParamType.STRING)
    private String status;
    private Integer isFirstCreate;

    public TagentVo() {
    }

    public TagentVo(JSONObject object) {
        this.id = object.getLong("agentId");
        this.ip = object.getString("ip");
        this.port = object.getInteger("port");
        this.name = object.getString("name");
        this.version = object.getString("version");
        this.runnerId = object.getLong("proxyId");
        this.runnerGroupId = object.getLong("proxyGroupId");
        this.runnerIp = object.getString("proxyIp");
        this.runnerPort = object.getString("proxyPort");
        this.pcpu = object.getString("pcpu");
        this.mem = object.getString("mem");
        this.status = object.getString("status");
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


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIpString() {
        return ipString;
    }

    public void setIpString(String ipString) {
        this.ipString = ipString;
    }

    public List<String> getIpList() {
        if (CollectionUtils.isEmpty(ipList) && StringUtils.isNotBlank(ipString)) {
            String[] ipStringArray = ipString.split(",");
            ipList = Arrays.asList(ipStringArray);
        }
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getOsId() {
        return osId;
    }

    public void setOsId(Long osId) {
        this.osId = osId;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsNameVersion() {
        return osName + File.separator + osVersion;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsbit() {
        return osbit;
    }

    public void setOsbit(String osbit) {
        this.osbit = osbit;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = RC4Util.decrypt(encryptKey, credential);
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public Long getRunnerGroupId() {
        return runnerGroupId;
    }

    public void setRunnerGroupId(Long runnerGroupId) {
        this.runnerGroupId = runnerGroupId;
    }

    public String getRunnerGroupName() {
        return runnerGroupName;
    }

    public void setRunnerGroupName(String runnerGroupName) {
        this.runnerGroupName = runnerGroupName;
    }

    public String getRunnerIp() {
        return runnerIp;
    }

    public void setRunnerIp(String runnerIp) {
        this.runnerIp = runnerIp;
    }

    public String getRunnerPort() {
        return runnerPort;
    }

    public void setRunnerPort(String runnerPort) {
        this.runnerPort = runnerPort;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPcpu() {
        return pcpu;
    }

    public void setPcpu(String pcpu) {
        this.pcpu = pcpu;
    }

    public String getMem() {
        return mem;
    }

    public void setMem(String mem) {
        this.mem = mem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDisConnectReason() {
        return disConnectReason;
    }

    public void setDisConnectReason(String disConnectReason) {
        this.disConnectReason = disConnectReason;
    }

    public Integer getIsFirstCreate() {
        return isFirstCreate;
    }

    public void setIsFirstCreate(Integer isFirstCreate) {
        this.isFirstCreate = isFirstCreate;
    }
}
