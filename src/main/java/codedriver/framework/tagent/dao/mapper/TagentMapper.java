package codedriver.framework.tagent.dao.mapper;

import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.tagent.dto.TagentOSVo;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.dto.TagentVo;

import java.util.List;

public interface TagentMapper {
    List<TagentVo> searchTagent(TagentVo tagentVo);

    List<String> searchTagentVersion();

    List<TagentOSVo> searchTagentOSType();

    List<RunnerGroupVo> searchTagentRunnerGroup();

    List<RunnerGroupVo> searchRunnerGroupInformation(RunnerGroupVo groupVo);

    int searchTagentCount();

    int searchTagentRunnerCount();

    TagentVo getTagentById(Long tagentId);

    int searchRunnerGroupCount();

    TagentVo selectTagentById(Long id);

    void deleteTagentById(Long id);

    List<TagentVersionVo> getVersionList();

    int updateTagentById(TagentVo tagent);

    TagentOSVo getOsByName(String toLowerCase);

    void insertOs(TagentOSVo newOS);

    Long getAccountIdById(Long id);
}
