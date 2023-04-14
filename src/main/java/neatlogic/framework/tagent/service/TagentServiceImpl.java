/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.framework.tagent.service;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.cmdb.crossover.IResourceAccountCrossoverMapper;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountBaseVo;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountIpVo;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountProtocolVo;
import neatlogic.framework.cmdb.dto.resourcecenter.IpVo;
import neatlogic.framework.cmdb.exception.resourcecenter.ResourceCenterAccountNotFoundException;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.common.util.IpUtil;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.RestVo;
import neatlogic.framework.dto.runner.NetworkVo;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.file.FileStorageMediumHandlerNotFoundException;
import neatlogic.framework.exception.file.FileTypeHandlerNotFoundException;
import neatlogic.framework.exception.runner.RunnerIdNotFoundException;
import neatlogic.framework.exception.runner.RunnerUrlIsNullException;
import neatlogic.framework.file.core.FileStorageMediumFactory;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.core.IFileStorageHandler;
import neatlogic.framework.file.core.IFileTypeHandler;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.*;
import neatlogic.framework.tagent.enums.TagentAction;
import neatlogic.framework.tagent.enums.TagentUpgradeStatus;
import neatlogic.framework.tagent.exception.*;
import neatlogic.framework.tagent.tagenthandler.core.ITagentHandler;
import neatlogic.framework.tagent.tagenthandler.core.TagentHandlerFactory;
import neatlogic.framework.util.RestUtil;
import neatlogic.module.framework.file.handler.LocalFileSystemHandler;
import neatlogic.module.framework.file.handler.MinioFileSystemHandler;
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

import static java.util.stream.Collectors.toList;

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
        if (tagentVo != null) {
            //保存tagent ostype
            if (tagentVo.getOsId() == null && StringUtils.isNotBlank(tagent.getOsType())) {
                TagentOSVo os = tagentMapper.getOsByName(tagent.getOsType().toLowerCase());
                if (os != null) {
                    tagent.setOsId(os.getId());
                } else {
                    TagentOSVo newOS = new TagentOSVo(tagent.getOsType());
                    tagentMapper.insertOs(newOS);
                    tagent.setOsId(newOS.getId());
                }
            }

            //保存tagent osbit
            if (StringUtils.isNotBlank(tagent.getOsbit())) {
                tagentMapper.insertOsBit(tagent.getOsbit());
            }
        }
        return tagentMapper.updateTagentById(tagent);
    }

    /**
     * 保存tagent、相关账号、协议，tagent的isFirstCreate==1则是新增，否则是更新
     *
     * @param tagent tagent
     * @return tagentId
     */
    @Override
    public Long saveTagentAndAccount(TagentVo tagent) {
        if (StringUtils.isBlank(tagent.getIp())) {
            throw new TagentIpNotFoundException(tagent);
        }

        //tagent的默认端口为3939，若不是3939则新增协议
        IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
        String protocolName;
        if (tagent.getPort() == 3939) {
            protocolName = "tagent";
        } else {
            protocolName = "tagent." + tagent.getPort();
        }
        AccountProtocolVo protocolVo = resourceAccountCrossoverMapper.getAccountProtocolVoByNameAndPort(protocolName, tagent.getPort());
        if (protocolVo == null) {
            protocolVo = new AccountProtocolVo(protocolName, tagent.getPort());
            resourceAccountCrossoverMapper.insertAccountProtocol(protocolVo);
        }

        List<String> newIpList = new ArrayList<>();
        List<String> insertTagentIpList = new ArrayList<>();

//        List<AccountVo> insertAccountList = new ArrayList<>();
//        List<AccountVo> updateAccountList = new ArrayList<>();
        List<AccountBaseVo> insertAccountList = new ArrayList<>();
        List<AccountBaseVo> updateAccountList = new ArrayList<>();
        if (tagent.getIsFirstCreate() != null && tagent.getIsFirstCreate() == 1) {
            //第一次注册tagent,主账号的ip、port肯定是不没有注册过的，因为一旦插入报错，证明前面ip逻辑的代码有问题
            AccountBaseVo accountVo = new AccountBaseVo(tagent.getIp() + "_" + tagent.getPort() + "_tagent", protocolVo.getId(), protocolVo.getPort(), tagent.getIp(), tagent.getCredential());
            insertAccountList.add(accountVo);
            //保存副ip账号逻辑
            saveTagentIpList(tagent, newIpList, insertTagentIpList, insertAccountList, updateAccountList, protocolVo);

            tagent.setAccountId(accountVo.getId());
            tagentMapper.insertTagent(tagent);
        } else {
            //重新注册tagent

            TagentVo oldTagentVo = tagentMapper.getTagentById(tagent.getId());
            AccountBaseVo newTagentAccountVo = new AccountBaseVo(tagent.getIp() + "_" + tagent.getPort() + "_tagent", protocolVo.getId(), protocolVo.getPort(), tagent.getIp(), tagent.getCredential());
            AccountBaseVo oldTagentAccount = tagentMapper.getAccountByTagentId(tagent.getId());

            /*主ip的账号逻辑
              根据tagentId找到的账号oldTagentAccount
              如果oldTagentAccount不为空：
                  oldTagentAccount的ip和现在注册的主ip是否相同
                      相同则是：情况1（使用原主ip注册）
                      不相同则是：情况2（使用副ip注册）
              如果oldTagentAccount为空：
                  根据ip、port再找一次账号，不存在则新增
              */
            if (oldTagentAccount != null) {
                if (StringUtils.equals(oldTagentAccount.getIp(), tagent.getIp())) {
                    //情况1：使用原主ip注册
                    newTagentAccountVo.setId(oldTagentAccount.getId());
                    if (!oldTagentAccount.equals(newTagentAccountVo)) {
                        updateAccountList.add(newTagentAccountVo);
                    }
                } else {
                    //情况2：使用副ip注册

                    AccountBaseVo oldAccountVo = tagentMapper.getResourceAccountByIpAndPort(tagent.getIp(), protocolVo.getPort());
                    newTagentAccountVo.setId(oldAccountVo.getId());
                    if (!newTagentAccountVo.equals(oldAccountVo)) {
                        updateAccountList.add(newTagentAccountVo);
                    }

                    //如果新包含ip不包含旧的主ip，这种情况确定要删除账号，直接删除原主ip账号信息、tagent_ip的记录
                    if (CollectionUtils.isEmpty(tagent.getIpList()) || !tagent.getIpList().contains(oldTagentAccount.getIp())) {
                        tagentMapper.deleteTagentIp(oldTagentVo.getId(), oldTagentVo.getIp());
                        AccountBaseVo oldIpAccountVo = tagentMapper.getResourceAccountByIpAndPort(oldTagentVo.getIp(), tagent.getPort());
                        if (oldIpAccountVo != null) {
                            Long accountId = oldIpAccountVo.getId();
                            tagentMapper.deleteAccountById(accountId);
                            tagentMapper.deleteAccountIpByAccountId(accountId);
//                            resourceAccountCrossoverMapper.deleteAccountById(accountId);
//                            resourceAccountCrossoverMapper.deleteResourceAccountByAccountId(accountId);
//                            resourceAccountCrossoverMapper.deleteAccountTagByAccountId(accountId);
//                            resourceAccountCrossoverMapper.deleteAccountIpByAccountId(accountId);
                        }
                    }
                }

            } else {
                //这情况是tagent表缺少accountId
                AccountBaseVo registeredTagentAccountVo = tagentMapper.getResourceAccountByIpAndPort(tagent.getIp(), tagent.getPort());
                if (registeredTagentAccountVo != null) {
                    newTagentAccountVo.setId(registeredTagentAccountVo.getId());
                    updateAccountList.add(newTagentAccountVo);
                } else {
                    insertAccountList.add(newTagentAccountVo);
                }
            }

            //保存副ip账号逻辑
            saveTagentIpList(tagent, newIpList, insertTagentIpList, insertAccountList, updateAccountList, protocolVo);

            tagent.setAccountId(newTagentAccountVo.getId());
            tagentMapper.updateTagentById(tagent);
        }

        return tagent.getId();
    }

    /**
     * 保存tagent包含ip列表
     *
     * @param tagent             注册tagent信息
     * @param newIpList          新的ip列表
     * @param insertTagentIpList 插入的tagent包含ip列表
     * @param insertAccountList  插入的账号列表
     * @param updateAccountList  更新的账号列表
     * @param protocolVo         协议vo
     */
    private void saveTagentIpList(TagentVo tagent, List<String> newIpList, List<String> insertTagentIpList, List<AccountBaseVo> insertAccountList, List<AccountBaseVo> updateAccountList, AccountProtocolVo protocolVo) {
//        IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);

        //如果包含ip列表包含了其他tagent的主ip，则注册失败
        if (CollectionUtils.isNotEmpty(tagent.getIpList())) {
            List<TagentVo> tagentVoList = tagentMapper.getTagentListByIpListAndPortAndTagentId(tagent.getIpList(), tagent.getPort(), tagent.getId());
            if (CollectionUtils.isNotEmpty(tagentVoList)) {
                throw new TagentIpListContainOtherTagentMainIpException(tagent, tagentVoList);
            }
            newIpList.addAll(tagent.getIpList());
        }
        List<String> oldIpList = tagentMapper.getTagentIpListByTagentId(tagent.getId());
        if (CollectionUtils.isNotEmpty(oldIpList)) {
            List<String> finalNewIpList = newIpList;
            List<String> deleteTagentIpList = oldIpList.stream().filter(item -> !finalNewIpList.contains(item)).collect(toList());
            insertTagentIpList = newIpList.stream().filter(item -> !oldIpList.contains(item)).collect(toList());
            deleteTagentIpList(deleteTagentIpList, tagent);

            //新增和更新账号
            oldIpList.removeAll(deleteTagentIpList);
            oldIpList.addAll(insertTagentIpList);

            for (String ip : oldIpList) {
                AccountBaseVo newAccountVo = new AccountBaseVo(ip + "_" + tagent.getPort() + "_tagent", protocolVo.getId(), protocolVo.getPort(), ip, tagent.getCredential());
//                AccountVo oldAccountVo = resourceAccountCrossoverMapper.getResourceAccountByIpAndPort(ip, protocolVo.getPort());
                AccountBaseVo oldAccountVo = tagentMapper.getAccountByIpAndPort(ip, protocolVo.getPort());
                if (oldAccountVo != null) {
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
                insertTagentIpList.addAll(newIpList);
                //查找相同ip port的账号，不存在的才需要新增
                List<String> sameIpList = tagentMapper.getAccountIpByIpListAndPort(newIpList, tagent.getPort());
                if (CollectionUtils.isNotEmpty(sameIpList)) {
                    newIpList = newIpList.stream().filter(item -> !sameIpList.contains(item)).collect(toList());
                }
                for (String ip : newIpList) {
                    AccountBaseVo newAccountVo = new AccountBaseVo(ip + "_" + tagent.getPort() + "_tagent", protocolVo.getId(), protocolVo.getPort(), ip, tagent.getCredential());
                    //避免重复插入已经插入的主ip账号
                    if (!insertAccountList.stream().map(AccountBaseVo::getIp).collect(Collectors.toList()).contains(ip)) {
                        insertAccountList.add(newAccountVo);
                    }
                }
            }
        }

        List<String> successInsertIpList = new ArrayList<>();
        List<String> insertAccountIpList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(insertAccountList)) {
            for (AccountBaseVo accountVo : insertAccountList) {
//                resourceAccountCrossoverMapper.insertAccount(accountVo);
                //insert ignore 返回0表示ignore，返回1表示insert,返回2表示update
                if (tagentMapper.insertAccount(accountVo) == 1) {
                    tagentMapper.insertAccountIp(new AccountIpVo(accountVo.getId(), accountVo.getIp()));
                    successInsertIpList.add(accountVo.getIp());
                }
            }
            insertAccountIpList = insertAccountList.stream().map(AccountBaseVo::getIp).collect(toList());
        }
        if (CollectionUtils.isNotEmpty(updateAccountList)) {
            for (AccountBaseVo accountVo : updateAccountList) {
                accountVo.setName(null);
//                resourceAccountCrossoverMapper.updateAccount(accountVo);
                tagentMapper.updateAccount(accountVo);
            }
        }

        //新增tagentIp
        if (CollectionUtils.isNotEmpty(insertTagentIpList)) {
            if (CollectionUtils.isNotEmpty(successInsertIpList)) {
                List<String> firstInsertAccountIpList = insertAccountIpList;
                // 情况1：!firstInsertAccountIpList.contains(e)属于包含账号复用的情况
                // 情况2：successInsertIpList.contains(e)属于避免注册并发重复插入的情况
                insertTagentIpList = insertTagentIpList.stream().filter(e -> !firstInsertAccountIpList.contains(e) || successInsertIpList.contains(e)).collect(toList());
                if (CollectionUtils.isNotEmpty(insertTagentIpList)) {
                    tagentMapper.insertTagentIp(tagent.getId(), insertTagentIpList);
                }
            }
        }
    }

    /**
     * 删除tagent的包含ip
     *
     * @param deleteTagentIpList 需要删除的tagent ipList
     * @param tagent             tagentVo
     */
    @Override
    public void deleteTagentIpList(List<String> deleteTagentIpList, TagentVo tagent) {
        if (CollectionUtils.isNotEmpty(deleteTagentIpList)) {
            //删除当前tagent需要删除的tagent ip
            for (String ip : deleteTagentIpList) {
                tagentMapper.deleteTagentIp(tagent.getId(), ip);
            }

            //找出其他tagent的相同的ip列表，这些ip的账号仍需保留使用
            List<String> sameIpList = tagentMapper.getTagentIpListByIpList(deleteTagentIpList);
//            IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
            //清除不存在的ip对应的账号
            for (String ip : deleteTagentIpList) {
                //存在情况：之前注册的ipList含有tagent的主ip，现在注册的ipList不含tagent的主ip，加此判断，防止误删
                if (StringUtils.equals(ip, tagent.getIp())) {
                    continue;
                }
                if (CollectionUtils.isNotEmpty(sameIpList) && sameIpList.contains(ip)) {
                    continue;
                }
                AccountBaseVo oldAccountVo = tagentMapper.getResourceAccountByIpAndPort(ip, tagent.getPort());
                if (oldAccountVo != null) {
                    Long accountId = oldAccountVo.getId();
                    tagentMapper.deleteAccountById(accountId);
                    tagentMapper.deleteAccountIpByAccountId(accountId);
//                    resourceAccountCrossoverMapper.deleteAccountById(accountId);
//                    resourceAccountCrossoverMapper.deleteResourceAccountByAccountId(accountId);
//                    resourceAccountCrossoverMapper.deleteAccountTagByAccountId(accountId);
//                    resourceAccountCrossoverMapper.deleteAccountIpByAccountId(accountId);
                }
            }
        }
    }

    @Override
    public List<TagentVo> getTagentList(TagentSearchVo tagentSearchVo) {

        List<TagentVo> returnTagentVoList = new ArrayList<>();

        if (CollectionUtils.isEmpty(tagentSearchVo.getIpPortList()) && CollectionUtils.isEmpty(tagentSearchVo.getNetworkVoList()) && CollectionUtils.isEmpty(tagentSearchVo.getRunnerGroupIdList())) {
            throw new TagentBatchActionCheckLessTagentIpAndPortException();
        }

        Set<Long> tagentIdSet = new HashSet<>();
        //ip：port
        if (CollectionUtils.isNotEmpty(tagentSearchVo.getIpPortList())) {
            List<TagentVo> tagentVoList = new ArrayList<>();
            for (IpVo ipVo : tagentSearchVo.getIpPortList()) {
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
        if (CollectionUtils.isNotEmpty(tagentSearchVo.getNetworkVoList())) {
            TagentVo tagentVo = new TagentVo();
            int tagentCount = tagentMapper.searchTagentCount(tagentVo);
            tagentVo.setPageSize(100);
            List<TagentVo> searchTagentList = new ArrayList<>();
            int pageCount = PageUtil.getPageCount(tagentCount, 100);
            for (int i = 1; i <= pageCount; i++) {
                tagentVo.setCurrentPage(i);
                searchTagentList = tagentMapper.searchTagent(tagentVo);
                for (TagentVo tagent : searchTagentList) {
                    for (NetworkVo networkVo : tagentSearchVo.getNetworkVoList()) {
                        if (IpUtil.isBelongSegment(tagent.getIp(), networkVo.getNetworkIp(), networkVo.getMask()) && !tagentIdSet.contains(tagent.getId())) {
                            returnTagentVoList.add(tagent);
                            tagentIdSet.add(tagent.getId());
                        }
                    }
                }
            }
        }

        //执行器组
        if (CollectionUtils.isNotEmpty(tagentSearchVo.getRunnerGroupIdList())) {
            List<TagentVo> tagentVoList = tagentMapper.getTagentListByRunnerGroupIdList(tagentSearchVo.getRunnerGroupIdList());
            if (CollectionUtils.isNotEmpty(tagentVoList)) {
                tagentIdSet.addAll(tagentVoList.stream().map(TagentVo::getId).collect(Collectors.toList()));
                returnTagentVoList.addAll(tagentVoList);
            }
        }
        return returnTagentVoList;
    }

    @Override
    public JSONObject batchExecTagentChannelAction(String action, List<TagentVo> tagentList, TagentMessageVo tagentMessageVo) throws Exception {
        JSONObject returnObj = new JSONObject();
        String space = "     ";
        Set<Long> runnerIdSet = tagentList.stream().map(TagentVo::getRunnerId).collect(Collectors.toSet());
        List<RunnerVo> runnerList = runnerMapper.getRunnerListByIdSet(runnerIdSet);
        //文件内容
        String fileDataString = "user：" + UserContext.get().getUserName() + space + "time：" + new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date()) + space + "tagentCount：" + tagentList.size() + "\n\n";
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
                    JSONObject resultObj = tagentHandler.execTagentCmd(tagentMessageVo, tagentVo, runnerVo);
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
                fileDataString = fileDataString + "The following tagent's heart beat not exist, there are " + heartbeatNotFoundTagentList.size() + " sets here：\n" + heartbeatNotFoundTagentList.stream().map(e -> e.getIp() + ":" + e.getPort()).collect(Collectors.joining(space)) + "\n\n";
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
    public JSONObject batchExecTagentChannelAction(String action, TagentSearchVo tagentSearchVo, TagentMessageVo tagentMessageVo) throws Exception {
        List<TagentVo> tagentList = getTagentList(tagentSearchVo);
        if (CollectionUtils.isNotEmpty(tagentList)) {
            return batchExecTagentChannelAction(action, tagentList, tagentMessageVo);
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
//            IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
            AccountBaseVo accountVo = tagentMapper.getAccountById(tagentVo.getAccountId());
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
