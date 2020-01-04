package com.ei312.ui.util;

import android.util.Log;

public class Logger {

    public enum LogLevel{
        FULL,
        NONE
    }

    private static LogLevel log_level = LogLevel.FULL;

    private static String getClassName(){
        String result;
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[2];
        result = thisMethodStack.getClassName();
        return result.substring(result.lastIndexOf("."),result.length());
    }

    private static String callMethodAndLine(){
        String result = "at ";
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[2];
        result += thisMethodStack.getClassName()+".";
        result += thisMethodStack.getMethodName();
        result += "("+thisMethodStack.getFileName();
        result += ":"+thisMethodStack.getLineNumber()+") ";
        return result;
    }

    public static void i(String msg){
        if(log_level== LogLevel.FULL)
            Log.i(getClassName(),callMethodAndLine()+msg);
    }

    public static void setLogLevel(LogLevel level){
        log_level = level;
    }

}
