package codedriver.framework.tagent.tagenthandler.core;

import codedriver.framework.applicationlistener.core.ModuleInitializedListenerBase;
import codedriver.framework.bootstrap.CodedriverWebApplicationContext;
import codedriver.framework.common.RootComponent;

import java.util.HashMap;
import java.util.Map;

@RootComponent
public class TagentHandlerFactory extends ModuleInitializedListenerBase {

    private static Map<String, ITagentHandler> tagentActionMap = new HashMap<>();

    public static ITagentHandler getInstance(String actionName) {
        return tagentActionMap.get(actionName);
    }


    @Override
    protected void onInitialized(CodedriverWebApplicationContext context) {
        Map<String, ITagentHandler> myMap = context.getBeansOfType(ITagentHandler.class);

        for (Map.Entry<String, ITagentHandler> entry : myMap.entrySet()) {
            ITagentHandler component = entry.getValue();
            if (component.getName() != null) {
                tagentActionMap.put(component.getName(), component);
            }
        }
    }

    @Override
    protected void myInit() {

    }
}
