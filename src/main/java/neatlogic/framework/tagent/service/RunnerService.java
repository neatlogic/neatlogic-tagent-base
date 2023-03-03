package neatlogic.framework.tagent.service;

import neatlogic.framework.dto.runner.RunnerVo;

public interface RunnerService {
    RunnerVo getRunnerById(Long runnerId);
}
