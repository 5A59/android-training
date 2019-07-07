package com.zy.commontec.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

class StubContentProvider : ContentProvider() {

    private var pluginProvider: ContentProvider? = null
    private var uriMatcher: UriMatcher? = UriMatcher(UriMatcher.NO_MATCH)

    override fun insert(uri: Uri?, values: ContentValues?): Uri? {
        loadPluginProvider()
        return pluginProvider?.insert(uri, values)
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        loadPluginProvider()
        if (isPlugin1(uri)) {
            return pluginProvider?.query(uri, projection, selection, selectionArgs, sortOrder)
        }
        return null
    }

    override fun onCreate(): Boolean {
        uriMatcher?.addURI("com.zy.stubprovider", "plugin1", 0)
        uriMatcher?.addURI("com.zy.stubprovider", "plugin2", 0)
        return true
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        loadPluginProvider()
        return pluginProvider?.update(uri, values, selection, selectionArgs) ?: 0
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        loadPluginProvider()
        return pluginProvider?.delete(uri, selection, selectionArgs) ?: 0
    }

    override fun getType(uri: Uri?): String {
        loadPluginProvider()
        return pluginProvider?.getType(uri) ?: ""
    }

    private fun loadPluginProvider() {
        if (pluginProvider == null) {
            pluginProvider = PluginUtils.classLoader?.loadClass("com.zy.plugin.PluginContentProvider")?.newInstance() as ContentProvider?
        }
    }

    private fun isPlugin1(uri: Uri?): Boolean {
        if (uriMatcher?.match(uri) == 0) {
            return true
        }
        return false
    }
}