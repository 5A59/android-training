package com.zy.hotfix.instant_run;

import java.util.HashMap;
import java.util.Map;

public class PatchInfo {
    public static Map<String, PatchRedirect> patchMap = new HashMap<>();
    public static void init() {
        patchMap.put("com.zy.hotfix.instant_run.InstantRunUtils", new InstantRunUtilsRedirect());
    }
}
