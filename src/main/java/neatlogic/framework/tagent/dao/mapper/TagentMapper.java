package neatlogic.framework.tagent.dao.mapper;

import neatlogic.framework.cmdb.dto.resourcecenter.AccountBaseVo;
import neatlogic.framework.dto.runner.GroupNetworkVo;
import neatlogic.framework.tagent.dto.TagentOSVo;
import neatlogic.framework.tagent.dto.TagentUpgradeAuditVo;
import neatlogic.framework.tagent.dto.TagentVersionVo;
import neatlogic.framework.tagent.dto.TagentVo;
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

    List<Long> getAccountIdListByIdList(List<Long> idList);

    TagentOSVo getOsByName(String name);

    TagentVo getTagentByIpAndPort(@Param("ip") String ip, @Param("port") Integer port);

    List<TagentVo> getTagentByIpOrTagentIpAndPort(@Param("ip") String ip, @Param("port") Integer port);

    List<TagentVo> getTagentByIncludeIpAndPort(@Param("ip") String ip, @Param("port") Integer port);

    TagentVo getTagentById(Long id);

    TagentVo getTagentDetailById(Long id);

    List<TagentVo> getTagentByAccountId(Long accountId);

    List<TagentVo> getTagentByIpList(@Param("ipList") List<String> ipList);

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

    AccountBaseVo getAccountByTagentId(Long id);

    AccountBaseVo getAccountById(Long id);

    List<AccountBaseVo> getAccountListByIncludeIpListAndProtocolId(@Param("ipList") List<String> ipList, @Param("protocolId") Long protocolId);

    List<AccountBaseVo> getAccountListByMainIpListAndProtocolId(@Param("ipList") List<String> ipList, @Param("protocolId") Long protocolId);

    void insertOs(TagentOSVo tagentOSVo);

    void insertOsBit(String osbit);

    void insertTagentIp(@Param("tagentId") Long tagentId, @Param("ipList") List<String> ipList);

    void insertAccount(AccountBaseVo newAccountVo);

    void replaceTagentPkgFile(TagentVersionVo versionVo);

    void insertUpgradeAudit(TagentUpgradeAuditVo audit);

    int insertTagent(TagentVo tagent);

    void insertTagentAuditDetail(TagentUpgradeAuditVo tagentAudit);

    void updateTagentAuditDetailStateAndResultById(@Param("id") Long id, @Param("status") String status, @Param("result") String result);

    int updateTagentById(TagentVo tagentVo);

    void updateAccount(AccountBaseVo accountVo);

    void deleteTagentById(Long id);

    void deleteTagentByIdList(List<Long> idList);

    int deleteAllIpByTagentId(Long id);

    int deleteAllIpByTagentIdList(List<Long> tagentIdList);

    void deleteTagentVersionById(Long id);

    void deleteTagentIp(@Param("tagentId") Long tagentId, @Param("ip") String ip);

    void deleteAccountById(Long id);

    void deleteAccountByIdList(List<Long> idList);

    void deleteAccountListByIdList(List<Long> idList);

}
