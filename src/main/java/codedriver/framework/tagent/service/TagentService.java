/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.framework.tagent.service;

import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.dto.TagentVo;
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
     * @param tagentVo tagentVo
     * @param versionVo 安装包vo
     * @param targetVersion 目标版本号
     * @param auditId 升级记录id
     */
    void batchUpgradeTagent(TagentVo tagentVo, TagentVersionVo versionVo, String targetVersion, Long auditId);

    /**
     * 获取对应的安装包版本
     *
     * @param tagentVo tagent
     * @param targetVersion 准备升级的版本号
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
     * @param tagent tagentVo
     */
    void deleteTagentIpList(List<String> deleteTagentIpList, TagentVo tagent);
}
