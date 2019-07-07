package com.zy.plugin

import android.content.ContentProvider
import android.content.ContentValues
import android.database.AbstractCursor
import android.database.Cursor
import android.net.Uri

class PluginContentProvider: ContentProvider() {

    override fun insert(uri: Uri?, values: ContentValues?): Uri? {
        return null
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        return object: AbstractCursor() {
            override fun getLong(column: Int): Long {
                return 1
            }

            override fun getCount(): Int {
                return 1
            }

            override fun getColumnNames(): Array<String> {
                return arrayOf("col1")
            }

            override fun getShort(column: Int): Short {
                return 1
            }

            override fun getFloat(column: Int): Float {
                return 1f
            }

            override fun getDouble(column: Int): Double {
                return 1.0
            }

            override fun isNull(column: Int): Boolean {
                return false
            }

            override fun getInt(column: Int): Int {
                return 1
            }

            override fun getString(column: Int): String {
                return "test"
            }
        }
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun getType(uri: Uri?): String {
        return ""
    }

}