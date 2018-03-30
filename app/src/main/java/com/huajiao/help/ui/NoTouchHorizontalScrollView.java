package com.huajiao.help.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class NoTouchHorizontalScrollView extends HorizontalScrollView {

	public NoTouchHorizontalScrollView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
	}
	public NoTouchHorizontalScrollView(Context context)
	{
		super(context);
	}
	
	public NoTouchHorizontalScrollView(Context context, AttributeSet attrs, int ar)
	{
		super(context, attrs, ar);
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return false;
	}
}
