package neatlogic.framework.tagent.auth.label;

import neatlogic.framework.auth.core.AuthBase;

import java.util.Collections;
import java.util.List;

public class TAGENT_MANAGE extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "Tagent管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "管理tagent";
	}

	@Override
	public String getAuthGroup() {
		return "framework";
	}

	@Override
	public Integer getSort() {
		return 2;
	}

	@Override
	public List<Class<? extends AuthBase>> getIncludeAuths(){
		return Collections.singletonList(TAGENT_BASE.class);
	}
}
