/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.framework.tagent.service;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.cmdb.crossover.IResourceAccountCrossoverMapper;
import codedriver.framework.cmdb.dto.resourcecenter.AccountIpVo;
import codedriver.framework.cmdb.dto.resourcecenter.AccountProtocolVo;
import codedriver.framework.cmdb.dto.resourcecenter.AccountVo;
import codedriver.framework.cmdb.dto.resourcecenter.IpVo;
import codedriver.framework.cmdb.exception.resourcecenter.ResourceCenterAccountNotFoundException;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.common.util.IpUtil;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.crossover.CrossoverServiceFactory;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.RestVo;
import codedriver.framework.dto.runner.NetworkVo;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.exception.file.FileStorageMediumHandlerNotFoundException;
import codedriver.framework.exception.file.FileTypeHandlerNotFoundException;
import codedriver.framework.exception.runner.RunnerIdNotFoundException;
import codedriver.framework.exception.runner.RunnerUrlIsNullException;
import codedriver.framework.file.core.FileStorageMediumFactory;
import codedriver.framework.file.core.FileTypeHandlerFactory;
import codedriver.framework.file.core.IFileStorageHandler;
import codedriver.framework.file.core.IFileTypeHandler;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.integration.authentication.enums.AuthenticateType;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.*;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.enums.TagentUpgradeStatus;
import codedriver.framework.tagent.exception.*;
import codedriver.framework.tagent.tagenthandler.core.ITagentHandler;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerFactory;
import codedriver.framework.util.RestUtil;
import codedriver.module.framework.file.handler.LocalFileSystemHandler;
import codedriver.module.framework.file.handler.MinioFileSystemHandler;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * @author lvzk
 * @since 2021/8/23 17:39
 **/
@Service
public class TagentServiceImpl implements TagentService {

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
                    TagentOSVo newOS = new TagentOSVo(tagentVo.getOsType());
                    tagentMapper.insertOs(newOS);
                    tagent.setOsId(newOS.getId());
                }
            }
        }
        return tagentMapper.updateTagentById(tagent);
    }

    @Override
    public Long saveTagentAndAccount(TagentVo tagent) {
        if (StringUtils.isBlank(tagent.getIp())) {
            throw new TagentIpNotFoundException(tagent);
        }
        IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
        AccountProtocolVo protocolVo = resourceAccountCrossoverMapper.getAccountProtocolVoByProtocolName("tagent");
        if (protocolVo == null) {
            resourceAccountCrossoverMapper.insertAccountProtocol(new AccountProtocolVo("tagent"));
            protocolVo = resourceAccountCrossoverMapper.getAccountProtocolVoByProtocolName("tagent");
        }
        List<String> oldIpList = tagentMapper.getTagentIpListByTagentIpAndPort(tagent.getIp(), tagent.getPort());
        List<String> newIpList = new ArrayList<>();
        List<String> deleteTagentIpList = new ArrayList<>();
        List<String> insertTagentIpList = new ArrayList<>();

        List<AccountVo> insertAccountList = new ArrayList<>();
        List<AccountVo> updateAccountList = new ArrayList<>();

        AccountVo account = new AccountVo(tagent.getIp() + "_" + tagent.getPort() + "_tagent", protocolVo.getId(), protocolVo.getPort(), tagent.getIp(), tagent.getCredential());
        AccountVo oldAccount = resourceAccountCrossoverMapper.getAccountByTagentIpAndPort(tagent.getIp(), tagent.getPort());
        if (oldAccount != null) {
            oldAccount.setIp(tagent.getIp());
            oldAccount.setProtocolId(protocolVo.getId());
            oldAccount.setProtocolPort(protocolVo.getPort());
            account.setId(oldAccount.getId());
            if (!oldAccount.equals(account)) {
                updateAccountList.add(account);
            }
        } else {
            insertAccountList.add(account);
        }
        tagent.setAccountId(account.getId());
        TagentVo oldTagent = tagentMapper.getTagentByIpAndPort(tagent.getIp(), tagent.getPort());
        if (oldTagent != null) {
            tagent.setId(oldTagent.getId());
            tagentMapper.updateTagentById(tagent);
        } else {
            tagent.setIsFirstCreate(1);
            tagentMapper.insertTagent(tagent);
        }

        if (CollectionUtils.isNotEmpty(tagent.getIpList())) {
            newIpList.addAll(tagent.getIpList());
        }
        if (CollectionUtils.isNotEmpty(oldIpList)) {
            deleteTagentIpList = oldIpList.stream().filter(item -> !newIpList.contains(item)).collect(toList());
            insertTagentIpList = newIpList.stream().filter(item -> !oldIpList.contains(item)).collect(toList());
            deleteTagentIpList(deleteTagentIpList, tagent);

            //新增和更新账号
            oldIpList.removeAll(deleteTagentIpList);
            oldIpList.addAll(insertTagentIpList);
            for (String ip : oldIpList) {
                AccountVo newAccountVo = new AccountVo(ip + "_" + tagent.getPort() + "_tagent", protocolVo.getId(), protocolVo.getPort(), ip, tagent.getCredential());
                AccountVo oldAccountVo = resourceAccountCrossoverMapper.getResourceAccountByIpAndPort(ip, protocolVo.getPort());
                if (oldAccountVo != null) {
                    oldAccountVo.setIp(ip);
                    oldAccountVo.setProtocolId(protocolVo.getId());
                    oldAccountVo.setProtocolPort(protocolVo.getPort());
                    newAccountVo.setId(oldAccountVo.getId());
                }
                if (oldAccountVo == null) {
                    insertAccountList.add(newAccountVo);
                } else if (!oldAccountVo.equals(newAccountVo)) {
                    updateAccountList.add(newAccountVo);
                }

            }
        } else {
            //新增账号
            if (CollectionUtils.isNotEmpty(newIpList)) {
                for (String ip : newIpList) {
                    AccountVo newAccountVo = new AccountVo(ip + "_" + tagent.getPort() + "_tagent", protocolVo.getId(), protocolVo.getPort(), ip, tagent.getCredential());
                    if (!insertAccountList.stream().map(AccountVo::getIp).collect(Collectors.toList()).contains(ip)) {
                        insertAccountList.add(newAccountVo);
                    }
                    insertTagentIpList.add(ip);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(insertAccountList)) {
            for (AccountVo accountVo : insertAccountList) {
                resourceAccountCrossoverMapper.insertAccount(accountVo);
                resourceAccountCrossoverMapper.insertAccountIp(new AccountIpVo(accountVo.getId(), accountVo.getIp()));
            }
        }
        if (CollectionUtils.isNotEmpty(updateAccountList)) {
            for (AccountVo accountVo : updateAccountList) {
                accountVo.setName(null);
                resourceAccountCrossoverMapper.updateAccount(accountVo);
            }
        }

        //新增tagentIp
        if (CollectionUtils.isNotEmpty(insertTagentIpList)) {
            tagentMapper.insertTagentIp(tagent.getId(), insertTagentIpList);
        }

        return tagent.getId();
    }

    @Override
    public void deleteTagentIpList(List<String> deleteTagentIpList, TagentVo tagent) {
        if (CollectionUtils.isNotEmpty(deleteTagentIpList)) {
            //清除不存在的ip
            for (String ip : deleteTagentIpList) {
                tagentMapper.deleteTagentIp(tagent.getId(), ip);
            }
            IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
            //清除不存在的ip对应的账号
            for (String ip : deleteTagentIpList) {
                //存在情况：之前注册的ipList含有tagent的ip，现在注册的ipList不含tagent的ip，加此判断，防止误删
                if (StringUtils.equals(ip, tagent.getIp())) {
                    continue;
                }
                AccountVo oldAccountVo = resourceAccountCrossoverMapper.getResourceAccountByIpAndPort(ip, tagent.getPort());
                if (oldAccountVo != null) {
                    Long accountId = oldAccountVo.getId();
                    resourceAccountCrossoverMapper.deleteAccountById(accountId);
                    resourceAccountCrossoverMapper.deleteResourceAccountByAccountId(accountId);
                    resourceAccountCrossoverMapper.deleteAccountTagByAccountId(accountId);
                    resourceAccountCrossoverMapper.deleteAccountIpByAccountId(accountId);
                }
            }
        }
    }

    @Override
    public List<TagentVo> getTagentList(List<IpVo> ipPortList, List<NetworkVo> networkVoList, List<Long> runnerGroupIdList) {

        List<TagentVo> returnTagentVoList = new ArrayList<>();

        if (CollectionUtils.isEmpty(ipPortList) && CollectionUtils.isEmpty(networkVoList) && CollectionUtils.isEmpty(runnerGroupIdList)) {
            throw new TagentBatchActionCheckLessTagentIpAndPortException();
        }

        Set<Long> tagentIdSet = new HashSet<>();
        //ip：port
        if (CollectionUtils.isNotEmpty(ipPortList)) {
            List<TagentVo> tagentVoList = new ArrayList<>();
            for (IpVo ipVo : ipPortList) {
                TagentVo tagentVo = tagentMapper.getTagentByIpAndPort(ipVo.getIp(), ipVo.getPort());
                if (tagentVo == null) {
                    continue;
                }
                tagentVoList.add(tagentVo);
                tagentIdSet.add(tagentVo.getId());
            }
            returnTagentVoList.addAll(tagentVoList);
        }

        //网段掩码
        if (CollectionUtils.isNotEmpty(networkVoList)) {
            TagentVo tagentVo = new TagentVo();
            int tagentCount = tagentMapper.searchTagentCount(tagentVo);
            tagentVo.setPageSize(100);
            List<TagentVo> searchTagentList = new ArrayList<>();
            int pageCount = PageUtil.getPageCount(tagentCount, 100);
            for (int i = 1; i <= pageCount; i++) {
                tagentVo.setCurrentPage(i);
                searchTagentList = tagentMapper.searchTagent(tagentVo);
                for (TagentVo tagent : searchTagentList) {
                    for (NetworkVo networkVo : networkVoList) {
                        if (IpUtil.isBelongSegment(tagent.getIp(), networkVo.getNetworkIp(), networkVo.getMask()) && !tagentIdSet.contains(tagent.getId())) {
                            returnTagentVoList.add(tagent);
                            tagentIdSet.add(tagent.getId());
                        }
                    }
                }
            }
        }

        //代理组
        if (CollectionUtils.isNotEmpty(runnerGroupIdList)) {
            List<TagentVo> tagentVoList = tagentMapper.getTagentListByRunnerGroupIdList(runnerGroupIdList);
            if (CollectionUtils.isNotEmpty(tagentVoList)) {
                tagentIdSet.addAll(tagentVoList.stream().map(TagentVo::getId).collect(Collectors.toList()));
                returnTagentVoList.addAll(tagentVoList);
            }
        }
        return returnTagentVoList;
    }

    @Override
    public JSONObject batchExecTagentChannelAction(String action, List<TagentVo> tagentList) throws Exception {
        JSONObject returnObj = new JSONObject();
        String space = "     ";
        Set<Long> runnerIdSet = tagentList.stream().map(TagentVo::getRunnerId).collect(Collectors.toSet());
        List<RunnerVo> runnerList = runnerMapper.getRunnerListByIdList(runnerIdSet);
        //文件内容（全部结果）
        String fileDataString = "user：" + UserContext.get().getUserName() + space + "time：" + new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date()) + space + "tagentCount：" + tagentList.size() + "\n\n";
        //返回部分结果（部分结果）
        if (CollectionUtils.isEmpty(runnerList)) {
            fileDataString = fileDataString + "All tagent's runner are not exist, there are " + tagentList.size() + " sets here：\n" + tagentList.stream().map(e -> e.getIp() + ":" + e.getPort()).collect(Collectors.joining(space)) + "\n\n";
            returnObj.put("runnerDisConnectTagentList", tagentList);
        } else {
            Map<Long, RunnerVo> runnerVoMap = runnerList.stream().collect(Collectors.toMap(RunnerVo::getId, e -> e));
            ITagentHandler tagentHandler = TagentHandlerFactory.getInstance(action);
            if (tagentHandler == null) {
                throw new TagentActionNotFoundException(action);
            }

            List<TagentVo> runnerNotFoundTagentList = new ArrayList<>();
            List<TagentVo> runnerDisConnectTagentList = new ArrayList<>();
            List<TagentVo> heartbeatNotFoundTagentList = new ArrayList<>();
            List<TagentVo> successTagentList = new ArrayList<>();
            for (TagentVo tagentVo : tagentList) {
                try {
                    RunnerVo runnerVo = runnerVoMap.get(tagentVo.getRunnerId());
                    if (runnerVo == null) {
                        runnerNotFoundTagentList.add(tagentVo);
                        continue;
                    }
                    JSONObject resultObj = tagentHandler.execTagentCmd(new TagentMessageVo(), tagentVo, runnerVo);
                    if (StringUtils.equals(resultObj.getString("Data"), "send command succeed")) {
                        successTagentList.add(tagentVo);
                    }
                } catch (TagentRunnerConnectRefusedException e) {
                    runnerDisConnectTagentList.add(tagentVo);
                } catch (TagentActionFailedException e) {
                    heartbeatNotFoundTagentList.add(tagentVo);
                }
            }

            if (CollectionUtils.isNotEmpty(runnerNotFoundTagentList)) {
                fileDataString = fileDataString + "The following tagent's  runner not exist, there are " + runnerNotFoundTagentList.size() + " sets here：\n" + runnerNotFoundTagentList.stream().map(e -> e.getIp() + ":" + e.getPort()).collect(Collectors.joining(space)) + "\n\n";
                returnObj.put("runnerNotFoundTagentList", runnerNotFoundTagentList);
            }
            if (CollectionUtils.isNotEmpty(runnerDisConnectTagentList)) {
                fileDataString = fileDataString + "The following tagent's runner is disconnected, there are " + runnerDisConnectTagentList.size() + " sets here：\n" + runnerDisConnectTagentList.stream().map(e -> e.getIp() + ":" + e.getPort()).collect(Collectors.joining(space)) + "\n\n";
                returnObj.put("runnerDisConnectTagentList", runnerDisConnectTagentList);
            }
            if (CollectionUtils.isNotEmpty(heartbeatNotFoundTagentList)) {
                fileDataString = fileDataString + "The following tagent's heart not exist, there are " + heartbeatNotFoundTagentList.size() + " sets here：\n" + heartbeatNotFoundTagentList.stream().map(e -> e.getIp() + ":" + e.getPort()).collect(Collectors.joining(space)) + "\n\n";
                returnObj.put("heartbeatNotFoundTagentList", heartbeatNotFoundTagentList);
            }
            if (CollectionUtils.isNotEmpty(successTagentList)) {
                fileDataString = fileDataString + "The following tagent " + action + " succeeded, there are " + successTagentList.size() + " sets here：\n" + successTagentList.stream().map(e -> e.getIp() + ":" + e.getPort()).collect(Collectors.joining(space)) + "\n\n";
                returnObj.put("successTagentList", successTagentList);
            }
        }

        //生成txt文件
        InputStream inputStream = new ByteArrayInputStream(fileDataString.getBytes(StandardCharsets.UTF_8));
        IFileTypeHandler fileTypeHandler = FileTypeHandlerFactory.getHandler("TAGENT");
        if (fileTypeHandler == null) {
            throw new FileTypeHandlerNotFoundException("TAGENT");
        }
        FileVo fileVo = new FileVo();
        fileVo.setName("tagentBatch" + action.substring(0, 1).toUpperCase() + action.substring(1) + "Results.txt");
        fileVo.setSize((long) inputStream.available());
        fileVo.setUserUuid(UserContext.get().getUserUuid());
        fileVo.setType("TAGENT");
        fileVo.setContentType("text/plain");
        if (fileTypeHandler.needSave()) {
            //始终只存最新的一份数据
            FileVo oldFileVo = fileMapper.getFileByNameAndUniqueKey(fileVo.getName(), null);
            String filePath;
            if (oldFileVo != null) {
                fileVo.setId(oldFileVo.getId());
            }
            try {
                filePath = FileUtil.saveData(MinioFileSystemHandler.NAME, TenantContext.get().getTenantUuid(), inputStream, fileVo.getId().toString(), fileVo.getContentType(), fileVo.getType());
            } catch (Exception ex) {
                // 如果minio出现异常，则上传到本地
                filePath = FileUtil.saveData(LocalFileSystemHandler.NAME, TenantContext.get().getTenantUuid(), inputStream, fileVo.getId().toString(), fileVo.getContentType(), fileVo.getType());
            }
            fileVo.setPath(filePath);
            if (oldFileVo == null) {
                fileMapper.insertFile(fileVo);
            } else {
                fileMapper.updateFile(fileVo);
            }
            returnObj.put("dataFileName", fileVo.getName());
            returnObj.put("dataFileUrl", "api/binary/file/download?id=" + fileVo.getId());
        }
        return returnObj;
    }

    @Override
    public JSONObject batchExecTagentChannelAction(String action, List<IpVo> ipVoList, List<NetworkVo> networkVoList, List<Long> runnerGroupIdList) throws Exception {
        List<TagentVo> tagentList = getTagentList(ipVoList, networkVoList, runnerGroupIdList);
        if (CollectionUtils.isNotEmpty(tagentList)) {
            return batchExecTagentChannelAction(action, tagentList);
        }
        return null;
    }

    @Override
    public void batchUpgradeTagent(TagentVo tagentVo, TagentVersionVo versionVo, String targetVersion, Long auditId) {

        String upgradeResult = StringUtils.EMPTY;
        boolean upgradeFlag = false;
        TagentUpgradeAuditVo tagentAudit = new TagentUpgradeAuditVo(auditId, tagentVo.getIp(), tagentVo.getPort(), tagentVo.getVersion(), targetVersion, TagentUpgradeStatus.WORKING.getValue());
        //插入此次升级记录详情
        tagentMapper.insertTagentAuditDetail(tagentAudit);
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
            IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
            AccountVo accountVo = resourceAccountCrossoverMapper.getAccountById(tagentVo.getAccountId());
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
            tagentAudit.setStatus(upgradeFlag ? TagentUpgradeStatus.SUCCEED.getValue() : TagentUpgradeStatus.FAILED.getValue());
            tagentAudit.setResult(upgradeResult);
            tagentMapper.updateTagentAuditDetailStateAndResultById(tagentAudit.getId(), tagentAudit.getStatus(), tagentAudit.getResult());
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
            throw new TagentActionNotFoundException(action);
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
            throw new RunnerUrlIsNullException(runnerVo.getId());
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
