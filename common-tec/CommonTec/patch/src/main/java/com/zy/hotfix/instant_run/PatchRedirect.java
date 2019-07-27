package com.zy.hotfix.instant_run;

public abstract class PatchRedirect {
    public abstract Object invokePatchMethod(String methodName, Object... params);
    public abstract boolean needPatch(String methodName);
}
