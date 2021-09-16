package codedriver.framework.tagent.auth.label;

import codedriver.framework.auth.core.AuthBase;

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
}
