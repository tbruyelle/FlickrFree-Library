package com.zmosoft.flickrfree;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommentLinkView extends Activity implements OnClickListener {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_link_view);
        
        RestClient.setAuth(this);
        
    	m_extras = getIntent().getExtras();
    	try {
			m_group_links = new JSONObject(m_extras.getString("groups"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

    	FillGroupList();
    }
    
    @SuppressWarnings("unchecked")
	private void FillGroupList() {
    	LinearLayout groupLayout = (LinearLayout)findViewById(R.id.LinkViewGroupsLayout);
		RelativeLayout entry;
		Iterator<String> i = m_group_links.keys();
    	while (i.hasNext()) {
    		entry = (RelativeLayout)View.inflate(this, R.layout.pools_list_item, null);
    		((TextView)entry.findViewById(R.id.PoolTitle)).setText(i.next());
    		((TextView)entry.findViewById(R.id.PoolNPhotos)).setText("");
    		
			entry.setClickable(true);
			entry.setOnClickListener(this);
    		groupLayout.addView(entry);
    	}
    }
    
	@Override
	public void onClick(View v) {
		if (v instanceof RelativeLayout) {
			String groupname = ((TextView)v.findViewById(R.id.PoolTitle)).getText().toString();
			try {
				String groupid = m_group_links.has(groupname) ? m_group_links.getString(groupname) : "";
				if (!groupid.equals("")) {
					Intent i = new Intent(this,ImageGrid.class);
					i.putExtra("group_id", groupid);
					i.putExtra("type", "pool");
					i.putExtra("title", groupname);
					startActivity(i);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	Bundle m_extras;

	JSONObject m_group_links;
}
