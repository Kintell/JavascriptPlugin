package com.kokakiwi.kintell.plugin.javascript.js;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaMethod;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.UniqueTag;
import org.mozilla.javascript.WrapFactory;

import com.kokakiwi.kintell.server.core.exec.annotations.NonAccessible;

public class SandboxNativeJavaObject extends NativeJavaObject
{
    private static final long serialVersionUID = -2287336938880657293L;
    
    public SandboxNativeJavaObject(Scriptable scope, Object javaObject,
            Class<?> staticType)
    {
        super(scope, javaObject, staticType);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object get(String name, Scriptable start)
    {
        if (name.equals("getClass"))
        {
            return NOT_FOUND;
        }
        
        Object o = super.get(name, start);
        
        if (o instanceof NativeJavaMethod)
        {
            try
            {
                NativeJavaMethod method = (NativeJavaMethod) o;
                Class<NativeJavaMethod> clazz = (Class<NativeJavaMethod>) method
                        .getClass();
                Field methodsField = clazz.getDeclaredField("methods");
                methodsField.setAccessible(true);
                
                Object[] methods = (Object[]) methodsField.get(o);
                Object javaMethod = methods[0];
                
                Class<?> methodClass = javaMethod.getClass();
                Field memberField = methodClass
                        .getDeclaredField("memberObject");
                memberField.setAccessible(true);
                Member member = (Member) memberField.get(javaMethod);
                
                if (member instanceof Method)
                {
                    Method m = (Method) member;
                    
                    if (m.isAnnotationPresent(NonAccessible.class))
                    {
                        o = UniqueTag.NOT_FOUND;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        return o;
    }
    
    public static class SandboxWrapFactory extends WrapFactory
    {
        @Override
        public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
                Object javaObject, Class<?> staticType)
        {
            return new SandboxNativeJavaObject(scope, javaObject, staticType);
        }
        
        @Override
        public Object wrap(Context cx, Scriptable scope, Object obj,
                Class<?> staticType)
        {
            return super.wrap(cx, scope, obj, staticType);
        }
    }
}
