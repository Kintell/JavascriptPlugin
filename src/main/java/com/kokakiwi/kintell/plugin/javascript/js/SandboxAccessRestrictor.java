package com.kokakiwi.kintell.plugin.javascript.js;

import org.mozilla.javascript.ClassShutter;

public class SandboxAccessRestrictor implements ClassShutter
{
    public final static String[] restrictedPackages = new String[] { "java.io" };
    
    public boolean visibleToScripts(String fullClassName)
    {
        boolean visible = true;
        
        for (final String restrictedPackage : restrictedPackages)
        {
            if (fullClassName.startsWith(restrictedPackage))
            {
                visible = false;
            }
        }
        
        return visible;
    }
}