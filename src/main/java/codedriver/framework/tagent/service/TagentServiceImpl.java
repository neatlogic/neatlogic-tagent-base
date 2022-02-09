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
import codedriver.framework.dto.RestVo;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.exception.file.FileStorageMediumHandlerNotFoundException;
import codedriver.framework.exception.runner.RunnerIdNotFoundException;
import codedriver.framework.exception.runner.RunnerUrlIllegalException;
import codedriver.framework.file.core.FileStorageMediumFactory;
import codedriver.framework.file.core.IFileStorageHandler;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.integration.authentication.enums.AuthenticateType;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.*;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.exception.*;
import codedriver.framework.tagent.tagenthandler.core.ITagentHandler;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerFactory;
import codedriver.framework.util.RestUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
        AccountProtocolVo protocolVo = resourceCenterMapper.getAccountProtocolVoByProtocolName("tagent");
        if (protocolVo == null) {
            resourceCenterMapper.insertAccountProtocol(new AccountProtocolVo("tagent"));
            protocolVo = resourceCenterMapper.getAccountProtocolVoByProtocolName("tagent");
        }
        tagent.setAccountId(saveTagentAccount(protocolVo, tagent.getIp(), tagent.getPort(), tagent.getCredential()));
        TagentVo oldTagent = tagentMapper.getTagentByIpAndPort(tagent.getIp(), tagent.getPort());
        if (oldTagent != null) {
            tagent.setId(oldTagent.getId());
        }
        tagentMapper.replaceTagent(tagent);
        //删除旧账号
        List<String> oldIpList = tagentMapper.getTagentIpListByTagentIpAndPort(tagent.getIp(), tagent.getPort());
        List<String> ipList = new ArrayList<>();
        ipList.addAll(tagent.getIpList());
        if (CollectionUtils.isNotEmpty(oldIpList)) {
            for (String ip : oldIpList) {
                AccountVo oldAccountVo = resourceCenterMapper.getAccountByName(ip + "_" + tagent.getPort() + "_tagent");
                if (oldAccountVo != null) {
                    if (StringUtils.equals(oldAccountVo.getPasswordPlain(),tagent.getCredential())) {
                        if (ipList.contains(ip)) {
                            ipList.remove(ip);
                        }
                        break;
                    }
                    Long accountId = oldAccountVo.getId();
                    resourceCenterMapper.deleteAccountById(accountId);
                    resourceCenterMapper.deleteResourceAccountByAccountId(accountId);
                    resourceCenterMapper.deleteAccountTagByAccountId(accountId);
                    resourceCenterMapper.deleteAccountIpByAccountId(accountId);
                }
            }
        }
        tagentMapper.deleteAllIpByTagentId(tagent.getId());
        if (CollectionUtils.isNotEmpty(tagent.getIpList())) {
            tagentMapper.insertTagentIp(tagent.getId(), tagent.getIpList());
            for (String ip : ipList) {
                saveTagentAccount(protocolVo, ip, tagent.getPort(), tagent.getCredential());
            }
        }
        return tagent.getId();
    }

    @Override
    public void batchUpgradeTagent(TagentVo tagentVo, TagentVersionVo versionVo, String targetVersion, Long auditId) {

        String upgradeResult = StringUtils.EMPTY;
        Boolean upgradeFlag = false;
        TagentUpgradeAuditVo tagentAudit = new TagentUpgradeAuditVo();
        tagentAudit.setAuditId(auditId);
        tagentAudit.setIp(tagentVo.getIp());
        tagentAudit.setPort(tagentVo.getPort());
        //插入此次升级记录详情
        tagentAudit.setSourceVersion(tagentVo.getVersion());
        tagentAudit.setTargetVersion(targetVersion);
        tagentAudit.setStatus("working");
        tagentMapper.replaceTagentAuditDetail(tagentAudit);
        try {
            if (versionVo == null) {
                throw new TagentPkgVersionAndDefaultVersionAreNotfoundException(targetVersion);
            }
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
                throw new RunnerIdNotFoundException(tagentVo.getRunnerId());
            }
            List<FileVo> fileVoList = new ArrayList<>();
            fileVoList.add(fileVo);
            JSONObject params = new JSONObject();
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
            RestVo restVo = new RestVo.Builder(runnerVo.getUrl() + "public/api/binary/tagent/upgrade", AuthenticateType.BASIC.getValue()).setFormData(params).setFileVoList(fileVoList).setContentType(RestUtil.MULTI_FORM_DATA).build();
            String resultStr = RestUtil.sendPostRequest(restVo);
            if (StringUtils.isNotBlank(resultStr)) {
                JSONObject resultObj = JSONObject.parseObject(resultStr);
                if (resultObj.getString("Status").equals("OK")) {
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
     *
     * @param tagentVo
     * @param targetVersion
     * @return
     */
    @Override
    public TagentVersionVo findTagentPkgVersion(TagentVo tagentVo, String targetVersion) {
        String osType = tagentVo.getOsType();
        String obsit = tagentVo.getOsbit();

        TagentVersionVo versionVo = tagentMapper.getTagentVersionVoByPkgVersionAndOSTypeAndOSBit(targetVersion, osType, obsit);

        if (versionVo == null) {
            versionVo = tagentMapper.getTagentVersionVoByPkgVersionAndOSTypeAndOSBit(targetVersion, osType, "default");
        }
/*
        //匹配最高版本
        if (isUsedHightestVersion && versionVo == null) {
            String newVersion = StringUtils.EMPTY;
            List<TagentVersionVo> versionVoList = tagentMapper.searchTagentPkgList(new TagentVersionVo(osType, obsit));
            List<String> versionList = versionVoList.stream().map(TagentVersionVo::getVersion).collect(Collectors.toList());
            newVersion = TagentVersionUtil.findHighestVersion(nowVersion, versionList);
            versionVo = tagentMapper.getTagentVersionVoByPkgVersionAndOSTypeAndOSBit(newVersion, osType, obsit);
        }*/

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
            throw new RunnerIdNotFoundException(tagentVo.getRunnerId());
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

    /**
     * 注册tagent关联的账号
     *
     * @param protocolVo
     * @param ip
     * @param port
     * @param credential
     * @return
     */
    private Long saveTagentAccount(AccountProtocolVo protocolVo, String ip, int port, String credential) {
        AccountVo account = new AccountVo();
        String accountName = ip + "_" + port + "_tagent";
        account.setProtocolId(protocolVo.getId());
        account.setName(accountName);
        account.setIp(ip);
        account.setProtocolPort(protocolVo.getPort());
        //account.setAccount(tagent.getUser()); 这是安装用户不是 账号名，tagent 账号为null
        account.setFcu(UserContext.get().getUserUuid());
        account.setPasswordPlain(credential);
        AccountVo oldAccount = resourceCenterMapper.getAccountByName(accountName);
        if (oldAccount != null) {
            account.setId(oldAccount.getId());
        }
        resourceCenterMapper.replaceAccount(account);
        resourceCenterMapper.insertIgnoreAccountIp(new AccountIpVo(account.getId(), account.getIp()));
        return account.getId();
    }
}
