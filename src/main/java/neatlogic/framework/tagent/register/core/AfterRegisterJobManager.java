/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
