package com.kokakiwi.kintell.plugin.javascript.js;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.kokakiwi.kintell.plugin.javascript.KintellJavascriptPlugin;
import com.kokakiwi.kintell.server.core.exec.Program;
import com.kokakiwi.kintell.server.core.exec.ProgramExecutor;

public class JSExecutor extends ProgramExecutor
{
    private final KintellJavascriptPlugin plugin;
    
    private Context                       context;
    private ScriptableObject              scope;
    
    private final File                    scriptFile;
    
    public JSExecutor(KintellJavascriptPlugin plugin, Program program)
    {
        super(program);
        this.plugin = plugin;
        
        scriptFile = new File(program.getRoot(), "script.js");
        if (!scriptFile.exists())
        {
            try
            {
                scriptFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        
        reset();
    }
    
    @Override
    public String getContentType()
    {
        return "text/javascript";
    }
    
    @Override
    public synchronized Object init()
    {
        Object result = null;
        
        final Object initObject = scope.get("init", scope);
        if (initObject != null && initObject != Scriptable.NOT_FOUND
                && initObject instanceof Function)
        {
            final Function initFunction = (Function) initObject;
            try
            {
                result = initFunction.call(context, scope, scope,
                        ScriptRuntime.emptyArgs);
            }
            catch (Exception e)
            {
                System.out
                        .println("Error in program '" + program.getId() + "'");
                e.printStackTrace();
            }
        }
        
        return result;
    }
    
    @Override
    public synchronized Object tick()
    {
        Object result = null;
        
        final Object tickObject = scope.get("tick", scope);
        if (tickObject != null && tickObject != Scriptable.NOT_FOUND
                && tickObject instanceof Function)
        {
            final Function tickFunction = (Function) tickObject;
            try
            {
                result = tickFunction.call(context, scope, scope,
                        ScriptRuntime.emptyArgs);
            }
            catch (Exception e)
            {
                System.out
                        .println("Error in program '" + program.getId() + "'");
                e.printStackTrace();
            }
        }
        return result;
    }
    
    @Override
    public void set(String name, Object value)
    {
        Object jsObject = Context.javaToJS(value, scope);
        ScriptableObject.putProperty(scope, name, jsObject);
    }
    
    @Override
    public void reset()
    {
        context = plugin.getContextFactory().enterContext();
        scope = context.initStandardObjects();
        
        context.setWrapFactory(new SandboxNativeJavaObject.SandboxWrapFactory());
        
        try
        {
            Reader reader = new FileReader(scriptFile);
            context.evaluateReader(scope, reader, "<cmd>", 1, null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public String getSource()
    {
        try
        {
            return IOUtils.toString(new FileReader(scriptFile));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return "";
    }
    
    @Override
    public void setSource(String source)
    {
        try
        {
            IOUtils.write(source, new FileOutputStream(scriptFile), "UTF-8");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public KintellJavascriptPlugin getPlugin()
    {
        return plugin;
    }
    
}
