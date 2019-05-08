package com.zy.plugin

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils;
import com.google.common.collect.Sets

/**
 * Created by zy on 2019/4/21.
 */

class MyTransform extends Transform {
    @Override
    String getName() {
        return "MyTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return Sets.immutableEnumSet(QualifiedContent.Scope.PROJECT)
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        // 在 transform 里，如果没有任何修改，也要把 input 的内容输出到 output，否则会丢失内容
        for (TransformInput input : transformInvocation.inputs) {
            input.directoryInputs.each { dir ->
                // 获取对应的输出目录
                File output = transformInvocation.outputProvider.getContentLocation(dir.name, dir.contentTypes, dir.scopes, Format.DIRECTORY)
                dir.changedFiles // 增量模式下修改的文件
                dir.file // 获取输入的目录
                FileUtils.copyDirectory(dir.file, output)
            }
            input.jarInputs.each { jar ->
                // 获取对应的输出 jar
                File output = transformInvocation.outputProvider.getContentLocation(jar.name, jar.contentTypes, jar.scopes, Format.JAR)
                jar.file // 获取输入的 jar 文件
                FileUtils.copyFile(jar.file, output)
            }
        }
    }
}
