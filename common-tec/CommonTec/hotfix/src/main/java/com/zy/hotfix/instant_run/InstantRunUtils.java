package com.zy.hotfix.instant_run;

import java.util.Map;

public class InstantRunUtils {
    public static PatchRedirect patchRedirect;

    public int getValue() {
        if (patchRedirect != null) {
            if (patchRedirect.needPatch("getValue")) {
                return (int) patchRedirect.invokePatchMethod("getValue");
            }
        }
        return 100;
    }

    public String getMsg() {
        if (patchRedirect != null) {
            if (patchRedirect.needPatch("getMsg")) {
                return (String) patchRedirect.invokePatchMethod("getMsg");
            }
        }
        return "value is: " + getValue();
    }

    public static void inject(ClassLoader classLoader) {
        try {
            Class patchInfoClass = classLoader.loadClass("com.zy.hotfix.instant_run.PatchInfo");
            patchInfoClass.getMethod("init").invoke(null);
            Map<String, Object> patchMap = (Map<String, Object>) patchInfoClass.getField("patchMap").get(null);
            for (String key: patchMap.keySet()) {
                PatchRedirect redirect = (PatchRedirect) patchMap.get(key);
                Class clazz = Class.forName(key);
                clazz.getField("patchRedirect").set(null, redirect);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unInject(ClassLoader classLoader) {
        try {
            Class patchInfoClass = classLoader.loadClass("com.zy.hotfix.instant_run.PatchInfo");
            patchInfoClass.getMethod("init").invoke(null);
            Map<String, Object> patchMap = (Map<String, Object>) patchInfoClass.getField("patchMap").get(null);
            for (String key: patchMap.keySet()) {
                Class clazz = Class.forName(key);
                clazz.getField("patchRedirect").set(null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
