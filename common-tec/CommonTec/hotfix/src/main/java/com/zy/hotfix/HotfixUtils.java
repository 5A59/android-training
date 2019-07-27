package com.zy.hotfix;

import android.content.Context;
import android.widget.Toast;

public class HotfixUtils {
    public void toast(Context context) {
        Toast.makeText(context, "hotfix message", Toast.LENGTH_SHORT).show();
    }
}
