package com.zy.module1;

import com.zy.module2.Module2Api;

/**
 * Created by zy on 2019/5/4.
 */

public class Module1Api {
    public void api1() {
        Module2Api api2 = new Module2Api();
        api2.api();
    }
}
