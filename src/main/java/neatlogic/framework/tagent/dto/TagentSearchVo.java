/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */
package neatlogic.framework.tagent.dto;

import neatlogic.framework.cmdb.dto.resourcecenter.IpVo;
import neatlogic.framework.dto.runner.NetworkVo;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @author longrf
 * @date 2022/9/19 15:41
 */

public class TagentSearchVo {

    @JSONField(serialize = false)
    String action; //动作
    @JSONField(serialize = false)
    List<IpVo> ipPortList; //ip端口列表
    @JSONField(serialize = false)
    List<NetworkVo> networkVoList; //网段掩码列表
    @JSONField(serialize = false)
    List<Long> runnerGroupIdList; //执行器组id列表

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<IpVo> getIpPortList() {
        return ipPortList;
    }

    public void setIpPortList(List<IpVo> ipPortList) {
        this.ipPortList = ipPortList;
    }

    public List<NetworkVo> getNetworkVoList() {
        return networkVoList;
    }

    public void setNetworkVoList(List<NetworkVo> networkVoList) {
        this.networkVoList = networkVoList;
    }

    public List<Long> getRunnerGroupIdList() {
        return runnerGroupIdList;
    }

    public void setRunnerGroupIdList(List<Long> runnerGroupIdList) {
        this.runnerGroupIdList = runnerGroupIdList;
    }
}
