package com.kokakiwi.kintell.plugin.javascript.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

public class JSContextFactory extends ContextFactory
{
    
    @Override
    public Context enterContext()
    {
        final Context context = super.enterContext();
        context.setWrapFactory(new SandboxNativeJavaObject.SandboxWrapFactory());
        return context;
    }
    
}
