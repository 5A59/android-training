package com.zy.hotfix.instant_run;

public class InstantRunUtilsRedirect extends PatchRedirect {
    @Override
    public Object invokePatchMethod(String methodName, Object... params) {
        if (methodName.equals("getValue")) {
            return getValue();
        }
        return null;
    }

    @Override
    public boolean needPatch(String methodName) {
        if ("getValue".equals(methodName)) {
            return true;
        }
        return false;
    }

    public int getValue() {
        return 200;
    }
}
