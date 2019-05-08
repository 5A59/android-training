package com.zy.easygradle;

import com.zy.module1.Module1Api;
//import com.zy.module2.Module2Api;

/**
 * Created by zy on 2019/5/4.
 */

public class ModuleApi {
    public void api() {
        Module1Api api = new Module1Api();
        api.api1();
//        Module2Api api2 = new Module2Api();
    }
}
