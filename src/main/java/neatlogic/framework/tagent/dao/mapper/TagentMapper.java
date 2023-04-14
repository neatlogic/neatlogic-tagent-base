package neatlogic.framework.tagent.dao.mapper;

import neatlogic.framework.cmdb.dto.resourcecenter.AccountBaseVo;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountIpVo;
import neatlogic.framework.dto.runner.GroupNetworkVo;
import neatlogic.framework.tagent.dto.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TagentMapper {
    List<TagentVo> searchTagent(TagentVo tagentVo);

    List<String> getAllTagentVersion();

    int searchTagentCount(TagentVo tagentVo);

    int searchTagentVersionCount();

    int searchTagentUpgradeAuditCountByUserName(String fcuName);

    int searchTagentUpgradeAuditDetailCountByAuditIdAndStatusAndIp(@Param("auditId") Long auditId, @Param("status") String status, @Param("ip") String ip);

    List<GroupNetworkVo> getGroupNetworkList();

    List<TagentVersionVo> searchTagentPkgList(TagentVersionVo tagentVersion);

    List<TagentUpgradeAuditVo> searchTagentUpgradeAuditList(TagentUpgradeAuditVo auditVo);

    List<TagentUpgradeAuditVo> searchTagentUpgradeAuditDetailList(TagentUpgradeAuditVo auditVo);

    Long getAccountIdById(Long id);

    TagentOSVo getOsByName(String name);

    TagentVo getTagentByIpAndPort(@Param("ip") String ip, @Param("port") Integer port);

    List<TagentVo> getTagentByIpOrTagentIpAndPort(@Param("ip") String ip, @Param("port") Integer port);

    TagentVo getTagentById(Long id);

    List<TagentVo> getTagentByAccountId(Long accountId);

    List<TagentVo> getTagentByIpList(@Param("ipList") List<String> ipList);

    List<TagentVo> getTagentListByAccountId(Long accountId);

    List<TagentVo> getTagentListByAccountIdList(List<Long> accountIdList);

    List<TagentVo> getTagentListByRunnerGroupIdList(List<Long> runnerGroupIdList);

    List<TagentVo> getTagentListByIpListAndPortAndTagentId(@Param("ipList") List<String> ipList, @Param("port") Integer port, @Param("tagentId") Long tagentId);

    List<TagentOSVo> getTagentOSTypeList();

    List<String> getTagentOsBitList();

    List<String> getTagentIpListByTagentId(Long id);

    TagentVersionVo getTagentVersionById(Long id);

    TagentVersionVo getTagentVersionVoByPkgVersionAndOSTypeAndOSBit(@Param("version") String version, @Param("osType") String osType, @Param("osbit") String osbit);

    List<String> getTagentIpListByTagentIpAndPort(@Param("ip") String ip, @Param("port") Integer port);

    List<String> getTagentIpListByIpList(@Param("ipList") List<String> ipList);

    int getTagentPkgFileIdUsedCount(Long fileId);

    Long getTagentIdByTagentIpAndPort(@Param("ip") String ip, @Param("port") Integer port);

    List<String> getAccountIpByIpListAndPort(@Param("ipList") List<String> ipList, @Param("port") Integer port);

    AccountBaseVo getAccountByIpAndPort(@Param("ip") String ip, @Param("port") Integer port);

    AccountBaseVo getAccountByTagentId(Long id);

    AccountBaseVo getResourceAccountByIpAndPort(@Param("ip") String ip, @Param("port") Integer port);

    AccountBaseVo getAccountById(Long id);

    List<AccountBaseVo> getAccountListByIpListAndProtocolId(@Param("ipList") List<String> ipList, @Param("protocolId") Long protocolId);

    void insertOs(TagentOSVo tagentOSVo);

    void insertOsBit(String osbit);

    void insertTagentIp(@Param("tagentId") Long tagentId, @Param("ipList") List<String> ipList);

    int insertAccount(AccountBaseVo newAccountVo);

    void replaceTagentPkgFile(TagentVersionVo versionVo);

    void insertUpgradeAudit(TagentUpgradeAuditVo audit);

    int insertTagent(TagentVo tagent);

    void insertTagentAuditDetail(TagentUpgradeAuditVo tagentAudit);

    void insertAccountIp(AccountIpVo accountIpVo);

    void updateTagentAuditDetailStateAndResultById(@Param("id") Long id, @Param("status") String status, @Param("result") String result);

    int updateTagentById(TagentVo tagentVo);

    void updateTagentStatusAndDisConnectReasonById(@Param("status") String status, @Param("disConnectReason") String disConnectReason, @Param("id") Long id);

    void updateAccount(AccountBaseVo accountVo);

    void deleteTagentById(Long id);

    int deleteAllIpByTagentId(Long id);

    void deleteTagentVersionById(Long id);

    void deleteTagentIp(@Param("tagentId") Long tagentId, @Param("ip") String ip);

    void deleteAccountById(Long id);

    void deleteAccountListByIdList(List<Long> idList);

    void deleteAccountIpByAccountId(Long accountId);

    void deleteAccountIpListByAccountIdList(List<Long> deletedAccountIdList);

}
