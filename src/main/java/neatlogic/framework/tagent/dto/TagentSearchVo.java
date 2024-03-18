/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/
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
