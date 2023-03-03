package neatlogic.framework.tagent.auth.label;

import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.label.RUNNER_MODIFY;

import java.util.Collections;
import java.util.List;

public class TAGENT_BASE extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "auth.framework.tagentbase.name";
	}

	@Override
	public String getAuthIntroduction() {
		return "auth.framework.tagentbase.introduction";
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
