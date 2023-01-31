/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.framework.tagent.register.core;

import neatlogic.framework.tagent.dto.TagentVo;

/**
 * tagent注册成功后需要执行的操作
 */
public abstract class AfterRegisterBase implements IAfterRegister {
    @Override
    public final void execute(TagentVo pTagentVo) {
        myExecute(pTagentVo);
    }

    protected abstract void myExecute(TagentVo tagentVo);
}
