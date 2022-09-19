/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.framework.tagent.service;

import codedriver.framework.cmdb.dto.resourcecenter.IpVo;
import codedriver.framework.dto.runner.NetworkVo;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.enums.TagentAction;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author lvzk
 * @since 2021/8/23 17:40
 **/
public interface TagentService {

    /**
     * 更新tagent信息（包括更新os信息，如果不存在os则insert后再绑定osId）
     *
     * @param tagentVo
     * @return
     */
    int updateTagentById(TagentVo tagentVo);

    /**
     * 保存tagent和相关账号
     *
     * @param tagentVo
     * @return
     */
    Long saveTagentAndAccount(TagentVo tagentVo);

    /**
     * 批量升级tagent
     *
     * @param tagentVo      tagentVo
     * @param versionVo     安装包vo
     * @param targetVersion 目标版本号
     * @param auditId       升级记录id
     */
    void batchUpgradeTagent(TagentVo tagentVo, TagentVersionVo versionVo, String targetVersion, Long auditId);

    /**
     * 获取对应的安装包版本
     *
     * @param tagentVo      tagent
     * @param targetVersion 目标版本号
     * @return 匹配到的安装包vo
     */
    TagentVersionVo findTagentPkgVersion(TagentVo tagentVo, String targetVersion);

    /**
     * 执行tagent action
     *
     * @param message 入参
     * @param action  执行动作
     */
    JSONObject execTagentCmd(TagentMessageVo message, String action) throws Exception;


    /**
     * 删除tagent 包含ip（ipList）
     *
     * @param deleteTagentIpList 需要删除的tagent ipList
     * @param tagent             tagentVo
     */
    void deleteTagentIpList(List<String> deleteTagentIpList, TagentVo tagent);

    /**
     * 通过ip：port、网段掩码、代理组 过滤tagent
     *
     * @param ipVoList          ip：port列表
     * @param networkVoList     网段掩码列表
     * @param runnerGroupIdList 代理组列表
     * @return tagent列表
     */
    List<TagentVo> getTagentList(List<IpVo> ipVoList, List<NetworkVo> networkVoList, List<Long> runnerGroupIdList);

    /**
     * 批量执行tagent心跳通道命令（现在支持的tagent心跳通道命令的有 reload(重启) resetcred(重置密码)）
     *
     * @param action     执行命令的动作
     * @param tagentList 需要执行的tagent列表
     * @return 执行结果
     * @throws Exception e
     */
    JSONObject batchExecTagentChannelAction(TagentAction action, List<TagentVo> tagentList) throws Exception;
}
