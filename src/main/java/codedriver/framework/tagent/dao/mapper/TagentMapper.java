package codedriver.framework.tagent.dao.mapper;

import codedriver.framework.dto.runner.GroupNetworkVo;
import codedriver.framework.tagent.dto.TagentOSVo;
import codedriver.framework.tagent.dto.TagentUpgradeAuditVo;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.dto.TagentVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TagentMapper {
    List<TagentVo> searchTagent(TagentVo tagentVo);

    List<String> searchTagentVersion();

    List<TagentOSVo> searchTagentOSType();

    int searchTagentCount(TagentVo tagentVo);

    int searchTagentVersionCount();

    int searchTagentUpgradeAuditCountByUserName(String fcuName);

    int searchTagentUpgradeAuditDetailCountByAuditIdAndStatusAndIp(@Param("auditId") Long auditId, @Param("status") String status, @Param("ip") String ip);

    List<GroupNetworkVo> getGroupNetworkList();

    List<TagentVersionVo> searchTagentPkgList(TagentVersionVo tagentVersion);

    List<TagentUpgradeAuditVo> searchTagenUpgradeAuditList(TagentUpgradeAuditVo auditVo);

    List<TagentUpgradeAuditVo> searchTagenUpgradeAuditDetailList(TagentUpgradeAuditVo auditVo);

    List<Long> searchTagentIdListByIpListAndPort(@Param("ipList") List<String> ipList, @Param("portList") List<String> portList);

    Long getAccountIdById(Long id);

    TagentOSVo getOsByName(String toLowerCase);

    TagentVo getTagentByIpAndPort(@Param("ip") String ip, @Param("port") Integer port);

    TagentVo getTagentById(Long id);

    List<TagentVo> getTagentByAccountId(Long accountId);

    List<TagentVo> getTagentByIpList(@Param("ipList") List<String> ipList);

    TagentVersionVo getTagentVersionById(Long id);

    TagentVersionVo getTagentVersionVoByPkgVersionAndOSTypeAndOSBit(@Param("version") String version, @Param("osType") String osType, @Param("osbit") String osbit);

    List<String> getTagentIpListByTagentIpAndPort(@Param("ip") String ip, @Param("port") Integer port);

    int getTagentPkgFileIdUsedCount(Long fileId);

    void insertOs(TagentOSVo newOS);

    void insertTagentIp(@Param("tagentId") Long tagentId, @Param("ipList") List<String> ipList);

    void replaceTagentPkgFile(TagentVersionVo versionVo);

    void insertUpgradeAudit(TagentUpgradeAuditVo audit);

    int replaceTagent(TagentVo tagent);

    void replaceTagentAuditDetail(TagentUpgradeAuditVo tagentAudit);

    int updateTagent(TagentVo tagentVo);

    int updateTagentById(TagentVo tagent);

    void deleteTagentById(Long id);

    int deleteAllIpByTagentId(Long id);

    void deleteTagentVersionById(Long id);

    void deleteTagentIpList(@Param("tagentId") Long tagentId, @Param("deleteTagentIpList") List<String> deleteTagentIpList);
}
