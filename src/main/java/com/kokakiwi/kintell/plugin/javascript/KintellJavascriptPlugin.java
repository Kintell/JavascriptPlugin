package com.kokakiwi.kintell.plugin.javascript;

import java.lang.reflect.Field;

import org.mozilla.javascript.NativeJavaMethod;

import com.kokakiwi.kintell.plugin.javascript.js.JSContextFactory;
import com.kokakiwi.kintell.server.plugins.ServerPlugin;

public class KintellJavascriptPlugin extends ServerPlugin
{
    private JSExecutorFactory      executorFactory;
    private final JSContextFactory contextFactory = new JSContextFactory();
    
    @Override
    public void onEnable()
    {
        executorFactory = new JSExecutorFactory(this);
        
        getCore().registerExecutorFactory(executorFactory);
    }
    
    public JSExecutorFactory getExecutorFactory()
    {
        return executorFactory;
    }
    
    public JSContextFactory getContextFactory()
    {
        return contextFactory;
    }
    
}
