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

package neatlogic.framework.tagent.service;

import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.dto.TagentSearchVo;
import neatlogic.framework.tagent.dto.TagentVersionVo;
import neatlogic.framework.tagent.dto.TagentVo;
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
     * 通过ip：port、网段掩码、执行器组 过滤tagent
     *
     * @param tagentSearchVo searchVo
     * @return tagent列表
     */
    List<TagentVo> getTagentList(TagentSearchVo tagentSearchVo);

    /**
     * 批量执行tagent心跳通道命令（现在支持的tagent心跳通道命令的有 reload(重启) resetcred(重置密码)、batchSaveConfig(批量修改配置文件)）
     *
     * @param action          执行命令的动作
     * @param tagentList      需要执行的tagent列表
     * @param tagentMessageVo tagent信息
     * @return 执行结果
     * @throws Exception e
     */
    JSONObject batchExecTagentChannelAction(String action, List<TagentVo> tagentList, TagentMessageVo tagentMessageVo) throws Exception;

    /**
     * 批量执行tagent心跳通道命令（现在支持的tagent心跳通道命令的有 reload(重启) resetcred(重置密码)、batchSaveConfig(批量修改配置文件)）
     *
     * @param action          执行命令的动作
     * @param tagentSearchVo  searchVo
     * @param tagentMessageVo tagent信息
     * @return 执行结果
     * @throws Exception e
     */
    JSONObject batchExecTagentChannelAction(String action, TagentSearchVo tagentSearchVo, TagentMessageVo tagentMessageVo) throws Exception;

}
