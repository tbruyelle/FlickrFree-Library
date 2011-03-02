package com.zmosoft.flickrfree;

import java.util.HashMap;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class CommentLayout extends LinearLayout {

	public CommentLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		m_group_links = new HashMap<String, String>();
	}
	
	public HashMap<String, String> m_group_links;
}

