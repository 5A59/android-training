package com.zy.architecture.mvc

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zy.architecture.R
import kotlinx.android.synthetic.main.architecture.*

/**
 * Created by zhangyi on 2019-09-14
 */

class MVCFragment : Fragment(), IView {

    companion object {
        fun newInstance(): Fragment {
            return MVCFragment()
        }
    }

    private val model: IModel = HandleModel()
    private var controller: IController = HandleController()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.architecture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setController(controller)
        model.setView(this)

        titleText.text = "MVC"
        edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                controller?.onDataChanged(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        clearText.setOnClickListener {
            controller?.clearData()
        }
    }

    override fun onDataHandled(data: String) {
        if (TextUtils.isEmpty(data)) {
            edit.setText("")
            msgText.text = "default msg"
        } else {
            msgText.text = data
        }
    }

    override fun dataHanding() {
        msgText.text = "handle data ..."
    }

    override fun setController(controller: IController) {
        this.controller = controller
    }
}
