/*
 * Copyright (C) 2017 Beijing Didi Infinity Technology and Development Co.,Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zy.commontec.activity.hook;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.lang.reflect.Method;

/**
 * Created by renyugang on 16/8/12.
 * 这个类直接拷贝 DynamicApk 里的实现
 */
public class PluginContext extends ContextWrapper {

    private Context context;
    private Application application;
    private ClassLoader classLoader;
    private Resources resources;
    private AssetManager assetManager;
    private Resources.Theme theme;
    private String pluginPath;

    public PluginContext(String pluginPath, Activity context, Application application, ClassLoader classLoader) {
        super(context);
        this.context = context;
        this.application = application;
        this.pluginPath = pluginPath;
        this.classLoader = classLoader;
        generateResources();
    }

    private void generateResources() {
        try {
            assetManager = AssetManager.class.newInstance();
            Method method = assetManager.getClass().getMethod("addAssetPath", String.class);
            method.invoke(assetManager, pluginPath);
            resources = new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context getApplicationContext() {
        return application;
    }

    private Context getHostContext() {
        return getBaseContext();
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public PackageManager getPackageManager() {
        return context.getPackageManager();
    }

    @Override
    public Resources getResources() {
        return resources;
    }

    @Override
    public AssetManager getAssets() {
        return assetManager;
    }

    @Override
    public Resources.Theme getTheme() {
        return theme;
    }
}
