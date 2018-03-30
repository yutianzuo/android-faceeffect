package com.huajiao.help.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class NoTouchScrollView extends ScrollView {

	public NoTouchScrollView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
	}
	public NoTouchScrollView(Context context)
	{
		super(context);
	}

	public NoTouchScrollView(Context context, AttributeSet attrs, int ar)
	{
		super(context, attrs, ar);
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return false;
	}
}
