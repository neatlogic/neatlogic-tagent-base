/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.framework.tagent.service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.cmdb.dao.mapper.resourcecenter.ResourceCenterMapper;
import codedriver.framework.cmdb.dto.resourcecenter.AccountIpVo;
import codedriver.framework.cmdb.dto.resourcecenter.AccountProtocolVo;
import codedriver.framework.cmdb.dto.resourcecenter.AccountVo;
import codedriver.framework.cmdb.exception.resourcecenter.ResourceCenterAccountNotFoundException;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.exception.file.FileStorageMediumHandlerNotFoundException;
import codedriver.framework.exception.runner.RunnerNotFoundException;
import codedriver.framework.exception.runner.RunnerUrlIllegalException;
import codedriver.framework.file.core.FileStorageMediumFactory;
import codedriver.framework.file.core.IFileStorageHandler;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.*;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.exception.*;
import codedriver.framework.tagent.tagenthandler.core.ITagentHandler;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerFactory;
import codedriver.framework.tagent.util.TagentHttpUtil;
import codedriver.framework.tagent.util.TagentVersionUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lvzk
 * @since 2021/8/23 17:39
 **/
@Service
public class TagentServiceImpl implements TagentService {

    @Resource
    ResourceCenterMapper resourceCenterMapper;

    @Resource
    TagentMapper tagentMapper;

    @Resource
    RunnerMapper runnerMapper;

    @Resource
    FileMapper fileMapper;

    @Override
    public int updateTagentById(TagentVo tagent) {
        TagentVo tagentVo = tagentMapper.getTagentById(tagent.getId());
        if (tagentVo != null && tagentVo.getOsId() == null) {
            if (StringUtils.isNotBlank(tagentVo.getOsType())) {
                TagentOSVo os = tagentMapper.getOsByName(tagentVo.getOsType().toLowerCase());
                if (os != null) {
                    tagent.setOsId(os.getId());
                } else {
                    TagentOSVo newOS = new TagentOSVo();
                    newOS.setName(tagentVo.getOsType());
                    tagentMapper.insertOs(newOS);
                    tagent.setOsId(newOS.getId());
                }
            }
        }
        return tagentMapper.updateTagentById(tagent);
    }

    @Override
    public Long saveTagent(TagentVo tagent) {
        if (StringUtils.isBlank(tagent.getIp())) {
            throw new TagentIpNotFoundException(tagent);
        }
        AccountVo account = new AccountVo();
        AccountProtocolVo protocolVo = resourceCenterMapper.getAccountProtocolVoByProtocolName("tagent");
        if (protocolVo == null) {
            resourceCenterMapper.insertAccountProtocol(new AccountProtocolVo("tagent"));
            protocolVo = resourceCenterMapper.getAccountProtocolVoByProtocolName("tagent");
        }
        String accountName = tagent.getIp() + "_" + tagent.getPort() + "_tagent";
        account.setProtocolId(protocolVo.getId());
        account.setName(accountName);
        account.setIp(tagent.getIp());
        account.setProtocolPort(protocolVo.getPort());
        //account.setAccount(tagent.getUser()); 这是安装用户不是 账号名，tagent 账号为null
        account.setFcu(UserContext.get().getUserUuid());
        account.setPasswordPlain(tagent.getCredential());
        AccountVo oldAccount = resourceCenterMapper.getAccountByName(accountName);
        if (oldAccount != null) {
            account.setId(oldAccount.getId());
        }
        resourceCenterMapper.replaceAccount(account);
        resourceCenterMapper.insertIgnoreAccountIp(new AccountIpVo(account.getId(),account.getIp()));
        TagentVo oldTagent = tagentMapper.getTagentByIpAndPort(tagent.getIp(), tagent.getPort());
        if (oldTagent != null) {
            tagent.setId(oldTagent.getId());
        }
        tagent.setAccountId(account.getId());
        tagentMapper.replaceTagent(tagent);
        List<String> ipList = tagent.getIpList();
        if (CollectionUtils.isNotEmpty(ipList)) {
            tagentMapper.deleteAllIpByTagentId(tagent.getId());
            tagentMapper.insertTagentIp(tagent.getId(), ipList);
        }
        return tagent.getId();
    }

    @Override
    public void batchUpdradeTagent(TagentVo tagentVo, TagentVersionVo versionVo, Long auditId) {

        String upgradeResult = StringUtils.EMPTY;
        Boolean upgradeFlag = false;
        TagentUpgradeAuditVo tagentAudit = new TagentUpgradeAuditVo();
        tagentAudit.setAuditId(auditId);
        tagentAudit.setIp(tagentVo.getIp());
        tagentAudit.setPort(tagentVo.getPort());

        try {
            FileVo fileVo = fileMapper.getFileById(versionVo.getFileId());
            if (fileVo == null) {
                throw new TagentPkgNotFoundException(versionVo.getFileId());
            }
            String prefix = fileVo.getPath().split(":")[0];
            IFileStorageHandler fileStorageHandler = FileStorageMediumFactory.getHandler(prefix.toUpperCase());
            if (fileStorageHandler == null) {
                throw new FileStorageMediumHandlerNotFoundException(prefix);
            }
            if (!fileStorageHandler.isExit(fileVo.getPath())) {
                throw new TagentPkgNotFoundException();
            }

            RunnerVo runnerVo = runnerMapper.getRunnerById(tagentVo.getRunnerId());
            if (runnerVo == null) {
                throw new RunnerNotFoundException(tagentVo.getRunnerId());
            }

            //插入此次升级记录详情
            tagentAudit.setSourceVersion(tagentVo.getVersion());
            tagentAudit.setTargetVersion(versionVo.getVersion());
            tagentAudit.setStatus("working");
            tagentMapper.replaceTagentAuditDetail(tagentAudit);

            if (TagentVersionUtil.compareVersion(tagentVo.getVersion(), versionVo.getVersion()) >= 0) {
                upgradeResult = "当前版本:" + versionVo.getVersion() + "与目标版本相同或更高，无需升级";
                throw new TagentPkgVersionIsHighestVersionException(tagentVo.getVersion());
            }

            List<FileVo> fileVoList = new ArrayList<>();
            fileVoList.add(fileVo);
            Map<String, String> params = new HashMap<>();
            params.put("type", TagentAction.UPGRADE.getValue());
            params.put("ip", tagentVo.getIp());
            params.put("port", (tagentVo.getPort()).toString());
            params.put("user", tagentVo.getUser());
            params.put("credential", tagentVo.getCredential());
            params.put("fileName", fileVo.getName());
            params.put("ignoreFile", versionVo.getIgnoreFile());
            AccountVo accountVo = resourceCenterMapper.getAccountById(tagentVo.getAccountId());
            if (accountVo == null) {
                throw new ResourceCenterAccountNotFoundException();
            }
            params.put("credential", accountVo.getPasswordCipher());

            String resultStr = new String(TagentHttpUtil.postFileWithParam(runnerVo.getUrl() + "public/api/binary/tagent/upgrade", params, fileVoList));
            if (StringUtils.isNotBlank(resultStr)) {
                JSONObject resultObj = JSONObject.parseObject(resultStr);
                if (resultObj.getString("Status").equals("OK")) {//需要runner返回的字段
                    upgradeResult = "升级成功";
                    upgradeFlag = true;
                } else {
                    upgradeResult = resultStr;
                }
            }
        } catch (Exception e) {
            upgradeResult = e.getMessage();
        } finally {
            tagentAudit.setStatus(upgradeFlag ? "successed" : "failed");
            tagentAudit.setResult(upgradeResult);
            tagentMapper.replaceTagentAuditDetail(tagentAudit);
        }
    }

    /**
     * 1、寻找对应的安装包（安装包由版本号、os类型、和CPU架构确定）
     * 2、若无对应版本的安装包，则寻找使用“default”标记并对应版本号的安装包（如版本号、os类型一样cpu架构是default的安装包，os类型和cpu架构均可以使用“default”标记）
     * 3、若无对应的default标记的对应版本号的安装包，则使用最高版本的安装包
     *
     * @param tagentVo
     * @param targetVersion
     * @param isUsedHightestVersion
     * @return
     */
    @Override
    public TagentVersionVo findTagentPkgVersion(TagentVo tagentVo, String targetVersion, Boolean isUsedHightestVersion) {
        String osType = this.getOsType(tagentVo.getOsType().toLowerCase(), tagentVo.getOsbit());
        String obsit = tagentVo.getOsbit();
        String nowVersion = tagentVo.getVersion();

        TagentVersionVo versionVo = tagentMapper.getTagentVersionVoByPkgVersionAndOSTypeAndOSBit(targetVersion, osType, obsit);

        if (versionVo == null) {
            versionVo = tagentMapper.getTagentVersionVoByPkgVersionAndOSTypeAndOSBit(targetVersion, "default", obsit);
            if (versionVo == null) {
                versionVo = tagentMapper.getTagentVersionVoByPkgVersionAndOSTypeAndOSBit(targetVersion, osType, "default");
                if (versionVo == null) {
                    versionVo = tagentMapper.getTagentVersionVoByPkgVersionAndOSTypeAndOSBit(targetVersion, "default", "default");
                }
            }
        }

        if (isUsedHightestVersion && versionVo == null) {
            String newVersion = StringUtils.EMPTY;
            List<TagentVersionVo> versionVoList = tagentMapper.searchTagentPkgList(new TagentVersionVo(osType, obsit));
            List<String> versionList = versionVoList.stream().map(TagentVersionVo::getVersion).collect(Collectors.toList());
            newVersion = TagentVersionUtil.findHighestVersion(nowVersion, versionList);
            versionVo = tagentMapper.getTagentVersionVoByPkgVersionAndOSTypeAndOSBit(newVersion, osType, obsit);
        }

        if (versionVo == null) {
            throw new TagentPkgVersionAndDefaultVersionAreNotfoundException(targetVersion);
        }
        return versionVo;
    }

    @Override
    public JSONObject execTagentCmd(TagentMessageVo message, String action) throws Exception {
        ITagentHandler tagentHandler = TagentHandlerFactory.getInstance(action);
        if (tagentHandler == null) {
            throw new TagentActionNotFoundEcexption(action);
        }
        TagentVo tagentVo = tagentMapper.getTagentById(message.getTagentId());
        if (tagentVo == null) {
            throw new TagentIdNotFoundException(message.getTagentId());
        }
        RunnerVo runnerVo = runnerMapper.getRunnerById(tagentVo.getRunnerId());
        if (runnerVo == null) {
            throw new RunnerNotFoundException(tagentVo.getRunnerId());
        }
        if (StringUtils.isBlank(runnerVo.getUrl())) {
            throw new RunnerUrlIllegalException(runnerVo.getId());
        }
        return tagentHandler.execTagentCmd(message, tagentVo, runnerVo);
    }

    /**
     * 根据os类型和CPU架构
     *
     * @param type
     * @param cpuBit
     * @return
     */
    private String getOsType(String type, String cpuBit) {
        String osType;
        if (type.equals("windows")) {
            if (cpuBit.contains("64")) {
                osType = TagentVersionVo.TagentOsType.WINDOWS64.getType();
            } else {
                osType = TagentVersionVo.TagentOsType.WINDOWS32.getType();
            }
        } else {
            osType = TagentVersionVo.TagentOsType.LINUX.getType();
        }
        return osType;
    }
}
