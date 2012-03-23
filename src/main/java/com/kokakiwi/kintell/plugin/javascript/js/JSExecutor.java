package com.kokakiwi.kintell.plugin.javascript.js;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.google.common.collect.Lists;
import com.kokakiwi.kintell.plugin.javascript.KintellJavascriptPlugin;
import com.kokakiwi.kintell.server.core.board.Result;
import com.kokakiwi.kintell.server.core.exec.Program;
import com.kokakiwi.kintell.server.core.exec.ProgramExecutor;

public class JSExecutor extends ProgramExecutor implements ErrorReporter
{
    private final KintellJavascriptPlugin plugin;
    
    private Context                       context;
    private ScriptableObject              scope;
    
    private final File                    scriptFile;
    
    private final List<String>            errors    = Lists.newLinkedList();
    private final Semaphore               semaphore = new Semaphore(1);
    
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
    public synchronized Result init()
    {
        Result result = new Result(Result.Type.OK);
        
        final Object initObject = scope.get("init", scope);
        if (initObject != null && initObject != Scriptable.NOT_FOUND
                && initObject instanceof Function)
        {
            errors.clear();
            
            try
            {
                final Function initFunction = (Function) initObject;
                initFunction.call(context, scope, scope,
                        ScriptRuntime.emptyArgs);
            }
            catch (Exception e)
            {
                
            }
            
            checkErrors(result);
        }
        
        return result;
    }
    
    @Override
    public synchronized Result tick()
    {
        Result result = new Result(Result.Type.OK);
        
        final Object tickObject = scope.get("tick", scope);
        if (tickObject != null && tickObject != Scriptable.NOT_FOUND
                && tickObject instanceof Function)
        {
            errors.clear();
            
            try
            {
                final Function tickFunction = (Function) tickObject;
                tickFunction.call(context, scope, scope,
                        ScriptRuntime.emptyArgs);
            }
            catch (Exception e)
            {
                
            }
            
            checkErrors(result);
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
    public Result reset()
    {
        Result result = new Result(Result.Type.OK);
        
        context = plugin.getContextFactory().enterContext();
        context.setErrorReporter(this);
        scope = context.initStandardObjects();
        context.setWrapFactory(new SandboxNativeJavaObject.SandboxWrapFactory());
        
        errors.clear();
        
        try
        {
            Reader reader = new FileReader(scriptFile);
            context.evaluateReader(scope, reader, "<cmd>", 1, null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            
        }
        
        checkErrors(result);
        
        return result;
    }
    
    public void checkErrors(Result result)
    {
        if (!errors.isEmpty())
        {
            result.setType(Result.Type.ERROR);
            result.getMessages().addAll(errors);
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
    
    public void warning(String message, String sourceName, int line,
            String lineSource, int lineOffset)
    {
        try
        {
            semaphore.acquire();
            errors.add(buildError(message, sourceName, line, lineSource,
                    lineOffset));
            semaphore.release();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    public void error(String message, String sourceName, int line,
            String lineSource, int lineOffset)
    {
        try
        {
            semaphore.acquire();
            errors.add(buildError(message, sourceName, line, lineSource,
                    lineOffset));
            semaphore.release();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    public EvaluatorException runtimeError(String message, String sourceName,
            int line, String lineSource, int lineOffset)
    {
        try
        {
            semaphore.acquire();
            errors.add(buildError(message, sourceName, line, lineSource,
                    lineOffset));
            semaphore.release();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        
        return new EvaluatorException(message, sourceName, line, lineSource,
                lineOffset);
    }
    
    public String buildError(String message, String sourceName, int line,
            String lineSource, int lineOffset)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[Error in '");
        sb.append(sourceName);
        sb.append("' at line ");
        sb.append(line);
        sb.append(" column ");
        sb.append(lineOffset);
        sb.append("] ");
        sb.append(message);
        
        return sb.toString();
    }
    
}
