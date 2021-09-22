package codedriver.framework.tagent.service;

import codedriver.framework.dto.runner.RunnerVo;

public interface RunnerService {
    RunnerVo getRunnerById(Long runnerId);
}
