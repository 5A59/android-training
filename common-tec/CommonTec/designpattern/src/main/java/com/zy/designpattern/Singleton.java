package com.zy.designpattern;

/**
 * Created by zhangyi on 2019-08-25
 */
public class Singleton {
    private static Singleton instance = new Singleton();

    private Singleton() {
    }

    public static synchronized Singleton getInstance() {
        return instance;
    }
}

//public class Singleton {
//    private static Singleton instance = null;
//
//    private Singleton() {
//    }
//
//    public static Singleton getInstance() {
//        if (instance == null) {
//            synchronized (Singleton.class) {
//                if (instance == null) {
//                    instance = new Singleton();
//                }
//            }
//        }
//        return instance;
//    }
//}
//
//public class Singleton {
//    private Singleton() {
//    }
//
//    public static Singleton getInstance() {
//        return SingletonHolder.instance;
//    }
//
//    private static class SingletonHolder {
//        private static final Singleton instance = new Singleton();
//    }
//}
//
//public enum SingletonEnum {
//    INSTANCE;
//}
