package com.zy.architecture.mvvm

import android.support.v4.app.Fragment

/**
 * Created by zhangyi on 2019-09-14
 */

class MVVMFragment : Fragment() {
    companion object {
        fun newInstance(): Fragment {
            return MVVMFragment()
        }
    }
}