/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.framework.tagent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import neatlogic.framework.asynchronization.threadlocal.MongodbSessionContext;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.cmdb.crossover.IResourceAccountCrossoverMapper;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountBaseVo;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountProtocolVo;
import neatlogic.framework.cmdb.dto.resourcecenter.IpVo;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.common.util.IpUtil;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.RestVo;
import neatlogic.framework.dto.runner.NetworkVo;
import neatlogic.framework.dto.runner.RunnerGroupVo;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.file.FileStorageMediumHandlerNotFoundException;
import neatlogic.framework.exception.file.FileTypeHandlerNotFoundException;
import neatlogic.framework.exception.runner.RunnerIdNotFoundException;
import neatlogic.framework.exception.runner.RunnerNotFoundByTagentIdException;
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
import neatlogic.framework.tagent.enums.TagentStatus;
import neatlogic.framework.tagent.enums.TagentUpgradeStatus;
import neatlogic.framework.tagent.exception.*;
import neatlogic.framework.tagent.tagenthandler.core.ITagentHandler;
import neatlogic.framework.tagent.tagenthandler.core.TagentHandlerFactory;
import neatlogic.framework.util.RestUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author lvzk
 * @since 2021/8/23 17:39
 **/
@Service
public class TagentServiceImpl implements TagentService {
    private final Logger logger = LoggerFactory.getLogger(TagentServiceImpl.class);
    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    TagentMapper tagentMapper;

    @Resource
    RunnerMapper runnerMapper;

    @Resource
    FileMapper fileMapper;

    @Override
    public void updateTagentById(TagentVo tagent) {
        TagentVo tagentVo = tagentMapper.getTagentById(tagent.getId());
        if (tagentVo != null) {
            //保存tagent ostype
            if (tagentVo.getOsId() == null && StringUtils.isNotBlank(tagent.getOsType())) {
                TagentOSVo os = tagentMapper.getOsByName(tagent.getOsType().toLowerCase());
                if (os != null) {
                    tagent.setOsId(os.getId());
                    tagent.setOsName(os.getName());
                } else {
                    TagentOSVo newOS = new TagentOSVo(tagent.getOsType());
                    tagentMapper.insertOs(newOS);
                    tagent.setOsId(newOS.getId());
                    tagent.setOsName(newOS.getName());
                }
                tagentMapper.updateTagentById(tagent);
            }

            //保存tagent osbit
            if (StringUtils.isNotBlank(tagent.getOsbit())) {
                tagentMapper.insertOsBit(tagent.getOsbit());
            }
        }
        //return tagentMapper.updateTagentById(tagent);
        updateTagentMGByIdWithLock(tagent, false);
    }

    @Override
    public void updateTagentMGByIdWithLock(TagentVo tagentVo, boolean isNeedInsert) {
        Long id = tagentVo.getId();
//        if (TransactionSynchronizationManager.isActualTransactionActive()) {
//            System.out.println("⚡ 当前在事务中！" + tagentVo.getId());
//            System.out.println("事务名称: " + TransactionSynchronizationManager.getCurrentTransactionName());
//            System.out.println("是否只读: " + TransactionSynchronizationManager.isCurrentTransactionReadOnly());
//            System.out.println("事务隔离级别: " + TransactionSynchronizationManager.getCurrentTransactionIsolationLevel());
//        }
        //必须串行，否则会导致mongodb多个事务同时改同一个document导致恶性竞争报错WriteConflict error
        tagentMapper.getTagentByIdLock(id);
        updateTagentMGById(tagentVo, isNeedInsert);
    }

    /**
     * 更新tagent状态等心跳信息
     *
     * @param tagentVo tagent对象
     */
    @Override
    public void updateTagentMGById(TagentVo tagentVo, boolean isNeedInsert) {
        Document whereDoc = new Document();
        Document doc = new Document();
        Document setDocument = new Document();
        whereDoc.put("id", tagentVo.getId());
        doc.put("id", tagentVo.getId());
        if (StringUtils.isNotBlank(tagentVo.getIp())) {
            doc.put("ip", tagentVo.getIp());
        }
        if (StringUtils.isNotBlank(tagentVo.getVersion())) {
            doc.put("version", tagentVo.getVersion());
        }
        if (tagentVo.getRunnerId() != null) {
            doc.put("runner_id", tagentVo.getRunnerId());
        }
        if (StringUtils.isNotBlank(tagentVo.getRunnerPort())) {
            doc.put("runner_port", tagentVo.getRunnerPort());
        }
        if (StringUtils.isNotBlank(tagentVo.getRunnerIp())) {
            doc.put("runner_ip", tagentVo.getRunnerIp());
        }
        if (tagentVo.getRunnerGroupId() != null) {
            doc.put("runner_group_id", tagentVo.getRunnerGroupId());
        }
        if (StringUtils.isNotBlank(tagentVo.getStatus())) {
            doc.put("status", tagentVo.getStatus());
        }
        if (StringUtils.isNotBlank(tagentVo.getPcpu())) {
            doc.put("pcpu", tagentVo.getPcpu());
        }
        if (StringUtils.isNotBlank(tagentVo.getMem())) {
            doc.put("mem", tagentVo.getMem());
        }
        doc.put("lcd", new Date());
        //origin
        if (tagentVo.getPort() != null) {
            doc.put("port", tagentVo.getPort());
        }
        if (StringUtils.isNotBlank(tagentVo.getName())) {
            doc.put("name", tagentVo.getName());
        }
        if (StringUtils.isNotBlank(tagentVo.getOsType())) {
            doc.put("os_type", tagentVo.getOsType());
        }
        if (StringUtils.isNotBlank(tagentVo.getOsName())) {
            doc.put("os_name", tagentVo.getOsName());
        }
        if (tagentVo.getOsId() != null) {
            doc.put("os_id", tagentVo.getOsId());
        }
        if (StringUtils.isNotBlank(tagentVo.getOsVersion())) {
            doc.put("os_version", tagentVo.getOsVersion());
        }
        if (StringUtils.isNotBlank(tagentVo.getOsbit())) {
            doc.put("osbit", tagentVo.getOsbit());
        }
        if (tagentVo.getAccountId() != null) {
            doc.put("account_id", tagentVo.getAccountId());
        }
        if (StringUtils.isNotBlank(tagentVo.getUser())) {
            doc.put("user", tagentVo.getUser());
        }
        if (StringUtils.isNotBlank(tagentVo.getDisConnectReason())) {
            doc.put("disconnect_reason", tagentVo.getDisConnectReason());
        }
        if (CollectionUtils.isNotEmpty(tagentVo.getIpList())) {
            doc.put("ip_list", tagentVo.getIpList());
        }
        setDocument.put("$set", doc);
        String whereDocStr = StringUtils.EMPTY;
        String setDocumentStr = StringUtils.EMPTY;
        String docDocumentStr = StringUtils.EMPTY;
        if (logger.isDebugEnabled()) {
            whereDocStr = JSON.toJSONString(whereDoc);
            setDocumentStr = JSON.toJSONString(setDocument);
        }
        logger.debug("====updateTagentMGById-thread-updated start! where:{}. updateDocument:{}", whereDocStr, setDocumentStr);
        if (isNeedInsert) {
            Criteria criteria = new Criteria();
            criteria.andOperator(Criteria.where("id").is(tagentVo.getId()));
            Query query = new Query(criteria);
            JSONObject oldData = mongoTemplate.findOne(query, JSONObject.class, "_tagent_info");
            //如果tagent不存在才insert
            if (oldData != null) {
                isNeedInsert = false;
            }
        }
        if (isNeedInsert) {
            InsertOneResult result;
            doc.put("id", tagentVo.getId());
            doc.put("fcd", new Date());
            if (logger.isDebugEnabled()) {
                docDocumentStr = JSON.toJSONString(doc);
            }
            try {
                result = mongoTemplate.getCollection("_tagent_info").insertOne(doc);
                // 判断更新结果
                if (result.getInsertedId() != null) {
                    // 更新成功
                    logger.debug("====updateTagentMGById-thread-updated insert with session succeed! where:{}, updateDocument:{}", whereDocStr, docDocumentStr);
                } else {
                    // 更新失败
                    whereDocStr = JSON.toJSONString(whereDoc);
                    setDocumentStr = JSON.toJSONString(setDocument);
                    logger.error("====updateTagentMGById-thread-updated insert without session failed! where:{}. updateDocument:{}", whereDocStr, docDocumentStr);
                }
            } catch (MongoWriteException e) {
                handleDuplicateIpPort(e, doc, docDocumentStr);
            }
        } else {
            UpdateResult result;
            try {
                result = mongoTemplate.getCollection("_tagent_info").updateOne(whereDoc, setDocument);
                // 判断更新结果
                if (result.getMatchedCount() > 0 && result.getModifiedCount() > 0) {
                    // 更新成功
                    logger.debug("====updateTagentMGById-thread-updated succeed! where:{}, updateDocument:{}", whereDocStr, setDocumentStr);
                } else {
                    // 更新失败
                    whereDocStr = JSON.toJSONString(whereDoc);
                    setDocumentStr = JSON.toJSONString(setDocument);
                    logger.error("====updateTagentMGById-thread-updated failed! where:{}. updateDocument:{}", whereDocStr, setDocumentStr);
                }
            } catch (MongoWriteException e) {
                handleDuplicateIpPort(e, doc, docDocumentStr);
            }
        }

        logger.debug("====updateTagentMGById-thread-updated done! where:{}. updateDocument:{},docDocumentStr:{}", whereDocStr, setDocumentStr, docDocumentStr);
    }

    /**
     * 处理ip port 冲突
     *
     * @param e              冲突异常
     * @param doc            更新文本对象
     * @param docDocumentStr 更新文本String
     */
    private void handleDuplicateIpPort(MongoWriteException e, Document doc, String docDocumentStr) {
        if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
            // 处理 (ip, port) 冲突
            deleteTagentMGById(doc.getLong("id"));
            Document conflict = mongoTemplate.getCollection("_tagent_info").find(
                    Filters.and(Filters.eq("ip", doc.getString("ip")), Filters.eq("port", doc.getInteger("port")))
            ).first();
            if (conflict != null) {
                logger.debug("====updateTagentMGById-thread-updated duplicate! where:{}.updateDocument:{}", conflict.getLong("id"), docDocumentStr);
                UpdateResult updateResult;
                doc.remove("_id");
                updateResult = mongoTemplate.getCollection("_tagent_info").updateOne(
                        Filters.eq("id", conflict.getLong("id")),
                        new Document("$set", doc)
                );

                // 判断更新结果
                if (updateResult.getMatchedCount() > 0 && updateResult.getModifiedCount() > 0) {
                    // 更新成功
                    logger.debug("====updateTagentMGById-thread-updated succeed! where:{}.updateDocument:{}", conflict.getLong("id"), docDocumentStr);
                } else {
                    // 更新失败
                    logger.error("====updateTagentMGById-thread-updated failed! where:{}.updateDocument:{}", conflict.getLong("id"), docDocumentStr);
                }
            }

        } else {
            throw e;
        }
    }

    /**
     * 根据tagentId 获取tagent心跳信息
     *
     * @param id tagentId
     * @return tagent对象
     */
    @Override
    public TagentVo getTagentMGById(long id) {
        Document whereDoc = new Document();
        whereDoc.put("id", id);
        TagentVo tagentVo = null;
        Document tagentDoc = mongoTemplate.getCollection("_tagent_info").find(whereDoc).first();
        if (tagentDoc != null) {
            tagentVo = mapToTagentVo(tagentDoc);
        }
        return tagentVo;
    }

    @Override
    public List<TagentVo> getTagentMGListByIdList(List<Long> idList) {
        List<TagentVo> tagentList = new ArrayList<>();
        Query query = new Query();
        query.addCriteria(Criteria.where("id").in(idList));
        List<Document> documentList = mongoTemplate.find(query, Document.class, "_tagent_info");
        if (CollectionUtils.isNotEmpty(documentList)) {
            for (Document doc : documentList) {
                TagentVo tagentVo = mapToTagentVo(doc);
                tagentList.add(tagentVo);
            }
        }
        return tagentList;
    }

    private TagentVo mapToTagentVo(Document doc) {
        TagentVo vo = new TagentVo();
        vo.setId(doc.getLong("id"));
        vo.setName(doc.getString("name"));
        vo.setIp(doc.getString("ip"));
        vo.setPort(doc.getInteger("port"));
        vo.setVersion(doc.getString("version"));
        vo.setRunnerId(doc.getLong("runner_id"));
        vo.setRunnerIp(doc.getString("runner_ip"));
        vo.setRunnerPort(doc.getString("runner_port"));
        vo.setRunnerGroupId(doc.getLong("runner_group_id"));
        vo.setStatus(doc.getString("status"));
        vo.setPcpu(doc.getString("pcpu"));
        vo.setMem(doc.getString("mem"));
        vo.setLcd(doc.getDate("lcd"));
        vo.setOsType(doc.getString("os_type"));
        vo.setOsName(doc.getString("os_name"));
        vo.setOsId(doc.getLong("os_id"));
        vo.setOsVersion(doc.getString("os_version"));
        vo.setOsbit(doc.getString("osbit"));
        vo.setAccountId(doc.getLong("account_id"));
        vo.setUser(doc.getString("user"));
        vo.setIpList(doc.getList("ip_list", String.class));
        return vo;
    }

    @Override
    public void deleteTagentMGById(long id) {
        Document whereDoc = new Document();
        whereDoc.put("id", id);
        DeleteResult result = mongoTemplate.getCollection("_tagent_info").deleteOne(whereDoc);
        logger.debug("====Tagent mongodb delete {} succeed! where:{}.", result.getDeletedCount(), whereDoc);
    }

    @Override
    public void deleteTagentMGByIpPort(String ip, int port) {
        Document whereDoc = new Document();
        whereDoc.put("ip", ip);
        whereDoc.put("port", port);
        DeleteResult result = mongoTemplate.getCollection("_tagent_info").deleteOne(whereDoc);
        logger.debug("====Tagent mongodb delete {} succeed! where:{}.", result.getDeletedCount(), whereDoc);
    }

    @Override
    public void deleteTagentMGByIdList(List<Long> idList) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").in(idList));
        mongoTemplate.remove(query, "_tagent_info");
    }

    @Override
    @Transactional
    public void deleteTagentByIdList(List<Long> idList, List<Long> accountIdList) {
        tagentMapper.deleteTagentByIdList(idList);
        tagentMapper.deleteAllIpByTagentIdList(idList);
        tagentMapper.deleteAccountByIdList(accountIdList);
    }

    /**
     * 根据tagentId列表获取runnerIdList
     *
     * @param tagentIdList tagentId列表
     * @return runnerIdSet
     */
    private Set<Long> getRunnerIdListByTagentMGIdList(List<Long> tagentIdList) {
        Document query = new Document("id", new Document("$in", tagentIdList));
        List<Document> documents = mongoTemplate.getCollection("_tagent_info")
                .find(query)
                .projection(new Document("runner_id", 1).append("_id", 0)) // 只返回 runner_id 字段
                .into(new ArrayList<>());

        Set<Long> runnerIdList = new HashSet<>();
        for (Document doc : documents) {
            Object runnerId = doc.get("runner_id");
            if (runnerId instanceof Number) { // 确保是数字类型
                runnerIdList.add(((Number) runnerId).longValue());
            }
        }
        return runnerIdList;
    }

    /**
     * 根据tagentId列表获取tagentId->runnerId的Map
     *
     * @param tagentIdList tagentId列表
     * @return runnerIdSet
     */
    private Map<Long, Long> getRunnerIdMapByTagentMGIdList(List<Long> tagentIdList) {
        Document query = new Document("id", new Document("$in", tagentIdList));
        List<Document> documents = mongoTemplate.getCollection("_tagent_info")
                .find(query)
                .projection(new Document("id", 1).append("runner_id", 1).append("_id", 0)) // 保留 id 和 runner_id 字段
                .into(new ArrayList<>());

        Map<Long, Long> resultMap = new HashMap<>();
        for (Document doc : documents) {
            Object idObj = doc.get("id");
            Object runnerIdObj = doc.get("runner_id");
            if (idObj instanceof Number && runnerIdObj instanceof Number) {
                Long id = ((Number) idObj).longValue();
                Long runnerId = ((Number) runnerIdObj).longValue();
                resultMap.put(id, runnerId);
            }
        }
        return resultMap;
    }


    /**
     * 根据tagentId列表获取runnerIdList
     *
     * @param tagentVo tagent 对象
     * @return runnerIdSet
     */
    @Override
    public List<TagentVo> searchTagentListMG(TagentVo tagentVo) {
        Criteria criteria = tagentMGCondition(tagentVo);
        Query query = new Query(criteria);
        int skip = (tagentVo.getCurrentPage() - 1) * tagentVo.getPageSize();
        query.fields()
                .include("id").include("ip").include("name").include("port")
                .include("version").include("runner_id").include("runner_ip")
                .include("runner_port").include("runner_group_id").include("status")
                .include("pcpu").include("mem").include("lcd").include("os_type")
                .include("os_name").include("os_id").include("os_version")
                .include("osbit").include("account_id").include("ip_list").include("user");
        query.skip(skip).limit(tagentVo.getPageSize());
        List<Document> documents = mongoTemplate.find(query, Document.class, "_tagent_info");
        List<TagentVo> tagentVoList = new ArrayList<>();
        for (Document doc : documents) {
            tagentVoList.add(mapToTagentVo(doc));
        }
        return tagentVoList;
    }

    private Criteria tagentMGCondition(TagentVo tagentVo) {
        List<Criteria> andCriteriaList = new ArrayList<>();

        // Status 条件
        if (Objects.equals(tagentVo.getStatus(), TagentStatus.CONNECTED.getValue())) {
            andCriteriaList.add(Criteria.where("status").is(tagentVo.getStatus()));
        } else if (Objects.equals(tagentVo.getStatus(), TagentStatus.DISCONNECTED.getValue())) {
            andCriteriaList.add(new Criteria().orOperator(
                    Criteria.where("status").is(tagentVo.getStatus()),
                    Criteria.where("status").exists(false)
            ));
        }

        // Version 条件
        if (StringUtils.isNotBlank(tagentVo.getVersion())) {
            andCriteriaList.add(Criteria.where("version").is(tagentVo.getVersion()));
        }

        // OsId 条件
        if (tagentVo.getOsId() != null) {
            andCriteriaList.add(Criteria.where("os_id").is(tagentVo.getOsId()));
        }

        //runnerGroup
        if (tagentVo.getRunnerGroupId() != null) {
            andCriteriaList.add(Criteria.where("runner_group_id").is(tagentVo.getRunnerGroupId()));
        }

        // Keyword 条件
        if (StringUtils.isNotBlank(tagentVo.getKeyword())) {
            String keyword = Pattern.quote(tagentVo.getKeyword()); // 防止特殊字符干扰正则
            andCriteriaList.add(new Criteria().orOperator(
                    Criteria.where("ip").regex(".*" + keyword + ".*", "i"),
                    Criteria.where("name").regex(".*" + keyword + ".*", "i"),
                    Criteria.where("os_version").regex(".*" + keyword + ".*", "i"),
                    Criteria.where("ip_list").elemMatch(Criteria.where("$regex").is(keyword))
            ));
        }
        Criteria criteria = new Criteria();
        // 将所有条件合并为一个 $and
        if (CollectionUtils.isNotEmpty(andCriteriaList)) {
            criteria.andOperator(andCriteriaList.toArray(new Criteria[0]));
        }
        return criteria;
    }

    @Override
    public void updateIpListMG(Long tagentId, List<String> newIpList) {
        ClientSession session = null;
        if (MongodbSessionContext.get() != null) {
            session = MongodbSessionContext.get().getSession();
        }
        Document whereDoc = new Document();
        whereDoc.put("id", tagentId);
        Document doc = new Document();
        doc.put("ip_list", newIpList);
        Document setDocument = new Document();
        setDocument.put("$set", doc);
        if (session != null) {
            mongoTemplate.getCollection("_tagent_info").updateOne(session, whereDoc, setDocument);
        } else {
            mongoTemplate.getCollection("_tagent_info").updateOne(whereDoc, setDocument);
        }
    }

    @Override
    public Long getTagentListMGCount(TagentVo tagentVo) {
        Criteria criteria = tagentMGCondition(tagentVo);
        Query query = new Query(criteria);
        return mongoTemplate.count(query, "_tagent_info");
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

        if (tagent.getIsFirstCreate() != null && tagent.getIsFirstCreate() == 1) {
            AccountBaseVo newTagentAccountVo = new AccountBaseVo(tagent.getIp() + "_" + tagent.getPort() + "_tagent", protocolVo.getId(), protocolVo.getPort(), tagent.getIp(), tagent.getCredential());
            //通过name复用账号
            AccountBaseVo oldTagentAccount = tagentMapper.getAccountByName(tagent.getIp() + "_" + tagent.getPort() + "_tagent");
            if (oldTagentAccount != null) {
                newTagentAccountVo.setId(oldTagentAccount.getId());
                if (!oldTagentAccount.equals(newTagentAccountVo)) {
                    tagentMapper.updateAccountByName(newTagentAccountVo);
                }
            } else {
                tagentMapper.insertAccount(newTagentAccountVo);
            }
            tagent.setAccountId(newTagentAccountVo.getId());
            tagentMapper.insertTagent(tagent);
            //防止并发ID得用数据库里面的
            TagentVo tagentVo = tagentMapper.getTagentByIpAndPort(tagent.getIp(), tagent.getPort());
            tagent.setId(tagentVo.getId());
            //存mongodb
            updateTagentMGByIdWithLock(tagent, true);
            //保存副ip
            saveTagentIpList(tagent);
        } else {
            //重新注册tagent
            AccountBaseVo newTagentAccountVo = new AccountBaseVo(tagent.getIp() + "_" + tagent.getPort() + "_tagent", protocolVo.getId(), protocolVo.getPort(), tagent.getIp(), tagent.getCredential());
            AccountBaseVo oldTagentAccount = tagentMapper.getAccountByTagentId(tagent.getId());
            if (oldTagentAccount != null) {
                //通过id复用账号
                newTagentAccountVo.setId(oldTagentAccount.getId());
                if (!oldTagentAccount.equals(newTagentAccountVo)) {
                    tagentMapper.updateAccountById(newTagentAccountVo);
                }
            } else {
                //通过name复用账号
                oldTagentAccount = tagentMapper.getAccountByName(tagent.getIp() + "_" + tagent.getPort() + "_tagent");
                if (oldTagentAccount != null) {
                    newTagentAccountVo.setId(oldTagentAccount.getId());
                    if (!oldTagentAccount.equals(newTagentAccountVo)) {
                        tagentMapper.updateAccountByName(newTagentAccountVo);
                    }
                } else {
                    tagentMapper.insertAccount(newTagentAccountVo);
                }
            }
            //存mongodb
            updateTagentMGByIdWithLock(tagent, true);
            //保存副ip
            saveTagentIpList(tagent);
            tagent.setAccountId(newTagentAccountVo.getId());
            tagentMapper.updateTagentById(tagent);
        }

        return tagent.getId();
    }

    /**
     * 保存tagent包含ip列表
     *
     * @param tagent 注册tagent信息
     */
    private void saveTagentIpList(TagentVo tagent) {
        List<String> newIpList = new ArrayList<>();
        List<String> insertTagentIpList = new ArrayList<>();
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
            List<String> deleteTagentIpList = oldIpList.stream().filter(item -> !newIpList.contains(item)).collect(toList());
            insertTagentIpList = newIpList.stream().filter(item -> !oldIpList.contains(item)).collect(toList());
            //删除当前tagent需要删除的tagent ip
            for (String ip : deleteTagentIpList) {
                tagentMapper.deleteTagentIp(tagent.getId(), ip);
            }
        } else {
            if (CollectionUtils.isNotEmpty(newIpList)) {
                insertTagentIpList.addAll(newIpList);
            }
        }
        //新增tagentIp
        if (CollectionUtils.isNotEmpty(insertTagentIpList)) {
            tagentMapper.insertTagentIp(tagent.getId(), insertTagentIpList);
        }
        updateIpListMG(tagent.getId(), tagent.getIpList());
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
                Map<Long, Long> tagentIdRunnerIdMap = getRunnerIdMapByTagentMGIdList(tagentVoList.stream().map(TagentVo::getId).collect(toList()));
                for (TagentVo tagentVo : tagentVoList) {
                    tagentVo.setRunnerId(tagentIdRunnerIdMap.get(tagentVo.getId()));
                }
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
        Set<Long> runnerIdSet = getRunnerIdListByTagentMGIdList(tagentList.stream().map(TagentVo::getId).collect(toList()));
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
            if (oldFileVo != null) {
                fileVo.setId(oldFileVo.getId());
            }
            String filePath = FileUtil.saveData(TenantContext.get().getTenantUuid(), inputStream, fileVo);
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
            TagentVo tagentMG = getTagentMGById(tagentVo.getId());
            if (tagentMG == null || tagentMG.getRunnerId() == null) {
                throw new RunnerNotFoundByTagentIdException(tagentVo.getId(), tagentVo.getIp());
            }
            RunnerVo runnerVo = runnerMapper.getRunnerById(tagentMG.getRunnerId());
            if (runnerVo == null) {
                throw new RunnerIdNotFoundException(tagentMG.getRunnerId());
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
                throw new TagentAccountNotFoundException(tagentVo.getAccountId());
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
        TagentVo tagentMG = getTagentMGById(tagentVo.getId());
        if (tagentMG == null || tagentMG.getRunnerId() == null) {
            throw new RunnerNotFoundByTagentIdException(tagentVo.getId(), tagentVo.getIp());
        }
        RunnerVo runnerVo = runnerMapper.getRunnerById(tagentMG.getRunnerId());
        if (runnerVo == null) {
            throw new RunnerIdNotFoundException(tagentMG.getRunnerId());
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

    @Override
    public void rollbackTagentMG(TagentVo tagentVo) {
        if (tagentVo != null) {
            TagentVo tagentVoRollBack = tagentMapper.getTagentById(tagentVo.getId());
            if (tagentVoRollBack != null) {
                tagentVoRollBack.setIpList(tagentMapper.getTagentIpListByTagentId(tagentVoRollBack.getId()));
                updateTagentMGByIdWithLock(tagentVoRollBack, true);
            } else {
                deleteTagentMGById(tagentVo.getId());
            }
        }
    }

    /**
     * 保存tagent
     *
     * @param tagentVo      入参
     * @param runnerGroupVo runner组
     */
    @Override
    public void saveTagent(TagentVo tagentVo, RunnerGroupVo runnerGroupVo) {
        tagentVo.setRunnerGroupId(runnerGroupVo.getId());
        //保存tagent osType
        if (StringUtils.isNotBlank(tagentVo.getOsType())) {
            String osType = tagentVo.getOsType();
            TagentOSVo os = tagentMapper.getOsByName(osType.toLowerCase());
            if (os != null) {
                tagentVo.setOsId(os.getId());
                tagentVo.setOsName(os.getName());
            } else {
                TagentOSVo newOS = new TagentOSVo(osType);
                tagentMapper.insertOs(newOS);
                tagentVo.setOsId(newOS.getId());
                tagentVo.setOsName(newOS.getName());
            }
        }

        //保存tagent osbit
        if (StringUtils.isNotBlank(tagentVo.getOsbit())) {
            tagentMapper.insertOsBit(tagentVo.getOsbit());
        }
        saveTagentAndAccount(tagentVo);
    }
}
