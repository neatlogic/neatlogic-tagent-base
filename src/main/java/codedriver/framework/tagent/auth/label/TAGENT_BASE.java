package codedriver.framework.tagent.auth.label;

import codedriver.framework.auth.core.AuthBase;
import codedriver.framework.auth.label.RUNNER_MODIFY;

import java.util.Collections;
import java.util.List;

public class TAGENT_BASE extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "Tagent基础权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "查看tagent";
	}

	@Override
	public String getAuthGroup() {
		return "framework";
	}

	@Override
	public Integer getSort() {
		return 1;
	}

	@Override
	public List<Class<? extends AuthBase>> getIncludeAuths(){
		return Collections.singletonList(RUNNER_MODIFY.class);
	}
}
