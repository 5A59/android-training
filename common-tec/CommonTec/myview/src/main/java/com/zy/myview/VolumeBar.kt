package com.zy.myview

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class VolumeBar constructor(context: Context?, attributes: AttributeSet?, defaultAttrStyle: Int) : LinearLayout(context, attributes, defaultAttrStyle) {
    constructor(context: Context?, attributes: AttributeSet?) : this(context, attributes, 0)
    constructor(context: Context?) : this(context, null)

    var count = 20
    var tip = ""
    var color = 0
    var viewList = ArrayList<VolumeView>()
    lateinit var text: TextView

    init {
        val typedArray = context?.obtainStyledAttributes(attributes, R.styleable.VolumeBar)
        tip = typedArray?.getString(R.styleable.VolumeBar_tip) ?: ""
        count = typedArray?.getInt(R.styleable.VolumeBar_count, 20) ?: 20
        color = typedArray?.getColor(R.styleable.VolumeBar_col_color, context.resources.getColor(R.color.colorPrimary))
                ?: context!!.resources.getColor(R.color.colorPrimary)
        typedArray?.recycle()  //注意回收
        gravity = Gravity.CENTER_VERTICAL
        initVolumeView()
    }

    private fun initVolumeView() {
        val params = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = 10
        (0 until count).forEach { _ ->
            val view = VolumeView(context)
            addView(view, params)
            viewList.add(view)
        }
        text = TextView(context)
        text.text = "$tip  0"
        addView(text, params)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                handleEvent(event)
            }
        }
        return true
    }

    // 触摸事件的处理逻辑，主要是在查找当前触摸事件的位置，确定是在第几个子 View 上，然后将此子 View 之前的所有子 View 都设置成实心的
    private fun handleEvent(event: MotionEvent) {
        val index = getCurIndex(event.x)
        if (index == -1) {
            viewList.forEach { view ->
                view.full = false
                view.invalidate()
            }
            text.text = "$tip  0"
            return
        }
        (0..index).forEach { i ->
            viewList[i].full = true
            viewList[i].invalidate()
        }
        (index + 1 until viewList.size).forEach { i ->
            viewList[i].full = false
            viewList[i].invalidate()
        }
        text.text = "$tip  ${index + 1}"
    }

    private fun getCurIndex(x: Float): Int {
        val pos = IntArray(2)
        var res = -1
        // 遍历子 View，确定当前触摸事件的位置
        viewList.forEachIndexed { index, view ->
            view.getLocationOnScreen(pos)
            if ((pos[0] + view.width) <= x) {
                res = index
            }
        }
        return res
    }
}
