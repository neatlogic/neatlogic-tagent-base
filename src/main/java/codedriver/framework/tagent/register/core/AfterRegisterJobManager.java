/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.framework.tagent.register.core;

import codedriver.framework.applicationlistener.core.ModuleInitializedListenerBase;
import codedriver.framework.bootstrap.CodedriverWebApplicationContext;
import codedriver.framework.common.RootComponent;
import codedriver.framework.tagent.dto.TagentVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RootComponent
public class AfterRegisterJobManager extends ModuleInitializedListenerBase {
    private static final List<IAfterRegister> afterRegisterList = new ArrayList<>();

    public static List<IAfterRegister> getAfterRegisterList() {
        return afterRegisterList;
    }

    /**
     * 执行所有注册后操作
     *
     * @param tagentVo tagent对象
     */
    public static void executeAll(TagentVo tagentVo) {
        for (IAfterRegister register : afterRegisterList) {
            register.execute(tagentVo);
        }
    }

    @Override
    protected void onInitialized(CodedriverWebApplicationContext context) {
        Map<String, IAfterRegister> map = context.getBeansOfType(IAfterRegister.class);
        for (Map.Entry<String, IAfterRegister> entry : map.entrySet()) {
            afterRegisterList.add(entry.getValue());
        }
    }

    @Override
    protected void myInit() {

    }
}
