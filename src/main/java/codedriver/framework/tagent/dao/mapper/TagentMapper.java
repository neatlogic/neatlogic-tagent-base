package codedriver.framework.tagent.dao.mapper;

import codedriver.framework.dto.runner.GroupNetworkVo;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.tagent.dto.TagentOSVo;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.dto.TagentVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TagentMapper {
    List<TagentVo> searchTagent(TagentVo tagentVo);

    List<String> searchTagentVersion();

    List<TagentOSVo> searchTagentOSType();

    List<RunnerGroupVo> searchTagentRunnerGroup();

    List<RunnerGroupVo> searchRunnerGroupInformation(RunnerGroupVo groupVo);

    int searchTagentCount(TagentVo tagentVo);

    int searchTagentRunnerCount();

    int searchRunnerGroupCount();

    int searchTagentVersionCount();

    List<TagentVersionVo> searchVersionList();

    List<GroupNetworkVo> getGroupNetworkList();

    List<TagentVersionVo> searchTagentPkgList();

    Long getAccountIdById(Long id);

    TagentOSVo getOsByName(String toLowerCase);

    TagentVo getTagentByIpAndPort(@Param("ip") String ip, @Param("port") Integer port);

    TagentVo getTagentById(Long id);

    TagentVersionVo getTagentVersionByFileId(Long id);

    int checkTagentVersion(String version);

    void insertOs(TagentOSVo newOS);

    void insertTagentIp(@Param("tagentId") Long tagentId, @Param("ipList") List<String> ipList);

    void insertTagentPkgFile(TagentVersionVo versionVo);

    int replaceTagent(TagentVo tagent);

    int updateTagent(TagentVo tagentVo);

    int updateTagentById(TagentVo tagent);

    void deleteTagentById(Long id);

    int deleteAllIpByTagentId(Long id);

    void deleteTagentVersionByFileId(Long id);
}
