package com.zy.myview

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

class MyView constructor(context: Context?, attributes: AttributeSet?, defaultAttrStyle: Int) : TextView(context, attributes, defaultAttrStyle) {
    constructor(context: Context?, attributes: AttributeSet?) : this(context, attributes, 0)
    constructor(context: Context?) : this(context, null)

    init {
        val typedArray = context?.obtainStyledAttributes(attributes, R.styleable.MyView)
        val message = typedArray?.getString(R.styleable.MyView_message)
        typedArray?.recycle()  //注意回收
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(100, 100)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        setFrame(0, 0, 100, 20)
        super.onLayout(changed, 0, 0, 100, 20)
    }
}