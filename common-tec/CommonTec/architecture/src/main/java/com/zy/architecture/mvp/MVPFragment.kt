package com.zy.architecture.mvp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zy.architecture.R
import kotlinx.android.synthetic.main.architecture.*

/**
 * Created by zhangyi on 2019-09-14
 */

class MVPFragment : Fragment(), IView {

    companion object {
        fun newInstance(): Fragment {
            val presenter = Presenter()
            val fragment = MVPFragment()
            val model = HandleModel()
            fragment.setPresenter(presenter)
            model.setPresenter(presenter)
            presenter.setModel(model)
            presenter.setView(fragment)
            return fragment
        }
    }

    var mpresenter: IPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.architecture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleText.text = "MVP"

        edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mpresenter?.onTextChanged(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        clearText.setOnClickListener {
            mpresenter?.onClearBtnClicked()
        }
    }

    override fun setPresenter(presenter: IPresenter) {
        this.mpresenter = presenter
    }

    override fun loading() {
        msgText.text = "handling data ..."
    }

    override fun showData(data: String) {
        msgText.text = data
    }
}
