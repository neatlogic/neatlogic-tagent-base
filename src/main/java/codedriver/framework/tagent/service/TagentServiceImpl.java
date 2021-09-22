/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.framework.tagent.service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.cmdb.dao.mapper.resourcecenter.ResourceCenterMapper;
import codedriver.framework.cmdb.dto.resourcecenter.AccountProtocolVo;
import codedriver.framework.cmdb.dto.resourcecenter.AccountVo;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentOSVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.exception.TagentIpNotFoundException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lvzk
 * @since 2021/8/23 17:39
 **/
@Transactional
@Service
public class TagentServiceImpl implements TagentService {

    @Resource
    ResourceCenterMapper resourceCenterMapper;

    @Resource
    TagentMapper tagentMapper;

    @Override
    public TagentVo getTagentById(Long id) {
        return tagentMapper.selectTagentById(id);
    }

    @Override
    public int updateTagentById(TagentVo tagent) {
        TagentVo tagentVo = tagentMapper.selectTagentById(tagent.getId());
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
        TagentVo oldTagent = tagentMapper.selectTagentByIpAndPort(tagent);
        AccountVo account = new AccountVo();
        AccountProtocolVo protocolVo = resourceCenterMapper.getAccountProtocolVoByProtocolName("tagent");
        if (protocolVo.getId() == null) {
            resourceCenterMapper.insertAccountProtocol(new AccountProtocolVo("tagent"));
            protocolVo = resourceCenterMapper.getAccountProtocolVoByProtocolName("tagent");
        }
        String accountName = tagent.getIp() + "_tagent";
        account.setProtocolId(protocolVo.getId());
        account.setName(accountName);
        account.setAccount(tagent.getUser());
        account.setFcu(UserContext.get().getUserUuid());
        account.setPasswordCipher(tagent.getCredential());//可否更简单
        AccountVo oldAccount = resourceCenterMapper.searchAccountByName(accountName);
        if (oldAccount == null) {  //账号信息操作
            resourceCenterMapper.insertAccount(account);
        } else {
            account.setId(oldAccount.getId());
            resourceCenterMapper.updateAccount(account);
        }
        if (oldTagent == null) {  //tagent信息操作
            tagent.setAccountId(resourceCenterMapper.getAccountByName(accountName).getId());
            tagentMapper.insertTagent(tagent);
        } else {
            tagent.setId(oldTagent.getId());
            tagent.setAccountId(resourceCenterMapper.getAccountByName(accountName).getId());
            tagentMapper.updateTagentByIpAndPort(tagent);
        }
        List<String> ipList = tagent.getIpList();
        if (CollectionUtils.isNotEmpty(ipList)) {
            tagentMapper.deleteAllIpByTagentId(tagent.getId());
            tagentMapper.insertTagentIp(tagent.getId(), ipList);
        }
        return tagent.getId();
    }
}
