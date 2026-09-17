package neatlogic.framework.tagent.auth.label;

import neatlogic.framework.auth.core.AuthBase;
import java.util.Collections;
import java.util.List;

/** 权限名称与说明使用国际化键，权限标识及校验规则保持不变。 */
public class TAGENT_MANAGE extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "auth.tagent_manage.name";
	}

	@Override
	public String getAuthIntroduction() {
		return "auth.tagent_manage.description";
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
