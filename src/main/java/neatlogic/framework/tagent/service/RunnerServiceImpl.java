package neatlogic.framework.tagent.service;

import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RunnerServiceImpl implements RunnerService {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public RunnerVo getRunnerById(Long runnerId) {
        return null;
    }
}
