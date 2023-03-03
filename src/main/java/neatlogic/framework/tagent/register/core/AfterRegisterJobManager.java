/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.framework.tagent.register.core;

import neatlogic.framework.applicationlistener.core.ModuleInitializedListenerBase;
import neatlogic.framework.bootstrap.NeatLogicWebApplicationContext;
import neatlogic.framework.common.RootComponent;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.transaction.core.AfterTransactionJob;

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
        AfterTransactionJob<TagentVo> job = new AfterTransactionJob<>("TAGENT-AFTER-REGISTER-HANDLER");
        job.execute(tagentVo, tagentVo1 -> {
            for (IAfterRegister register : afterRegisterList) {
                register.execute(tagentVo1);
            }
        });
    }

    @Override
    protected void onInitialized(NeatLogicWebApplicationContext context) {
        Map<String, IAfterRegister> map = context.getBeansOfType(IAfterRegister.class);
        for (Map.Entry<String, IAfterRegister> entry : map.entrySet()) {
            afterRegisterList.add(entry.getValue());
        }
    }

    @Override
    protected void myInit() {

    }
}
