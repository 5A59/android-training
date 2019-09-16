package com.zy.architecture

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.architecture.*

/**
 * Created by zhangyi on 2019-09-14
 */

class NormalFragment : Fragment() {
    companion object {
        fun newInstance(): Fragment {
            return NormalFragment()
        }
    }
    private val handler: Handler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.architecture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleText.text = "NORMAL"
        edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                handleData(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        clearText.setOnClickListener {
            edit.setText("")
        }
    }

    private fun handleData(data: String) {
        if (TextUtils.isEmpty(data)) {
            msgText.text = "default msg"
            return
        }
        msgText.text = "handle data ..."
        handler.removeCallbacksAndMessages(null)
        // 延迟来模拟网络或者磁盘操作
        handler.postDelayed({
            msgText.text = "handled data: $data"
        }, 3000)
    }
}
