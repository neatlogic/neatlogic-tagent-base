/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.framework.tagent.register.core;

import codedriver.framework.asynchronization.threadlocal.InputFromContext;
import codedriver.framework.common.constvalue.InputFrom;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.transaction.core.AfterTransactionJob;

/**
 * tagent注册成功后需要执行的操作
 */
public abstract class AfterRegisterBase implements IAfterRegister {
    @Override
    public final void execute(TagentVo pTagentVo) {
        InputFromContext.init(InputFrom.AUTOEXEC);
        AfterTransactionJob<TagentVo> job = new AfterTransactionJob<>();
        job.execute(pTagentVo, this::myExecute);
    }

    protected abstract void myExecute(TagentVo tagentVo);
}
