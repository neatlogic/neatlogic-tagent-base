package codedriver.framework.tagent.tagenthandler.core;

import codedriver.framework.applicationlistener.core.ModuleInitializedListenerBase;
import codedriver.framework.bootstrap.CodedriverWebApplicationContext;
import codedriver.framework.common.RootComponent;

import java.util.HashMap;
import java.util.Map;

@RootComponent
public class TagentHookFactory extends ModuleInitializedListenerBase {

    public static Map<String, TagentHook> componentMap = new HashMap<String, TagentHook>();

    public static TagentHook getComponent(String stepType) {
        if (!componentMap.containsKey(stepType) || componentMap.get(stepType) == null) {
            throw new RuntimeException("找不到类型为：" + stepType + "的流程组件");
        }
        return componentMap.get(stepType);
    }

    @Override
    protected void onInitialized(CodedriverWebApplicationContext context) {

    }

    @Override
    protected void myInit() {

    }
}
