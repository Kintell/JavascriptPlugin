package com.kokakiwi.kintell.plugin.javascript;

import com.kokakiwi.kintell.plugin.javascript.js.JSExecutor;
import com.kokakiwi.kintell.server.core.exec.Program;
import com.kokakiwi.kintell.server.core.exec.ProgramExecutorFactory;

public class JSExecutorFactory implements ProgramExecutorFactory<JSExecutor>
{
    private final KintellJavascriptPlugin plugin;
    
    public JSExecutorFactory(KintellJavascriptPlugin plugin)
    {
        this.plugin = plugin;
    }
    
    public String getId()
    {
        return "javascript";
    }
    
    public String getContentType()
    {
        return "text/javascript";
    }
    
    public String getName()
    {
        return "Javascript";
    }
    
    public JSExecutor createExecutor(Program program)
    {
        JSExecutor executor = new JSExecutor(plugin, program);
        
        return executor;
    }
    
}
