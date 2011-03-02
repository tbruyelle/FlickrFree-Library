package com.zmosoft.flickrfree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Groups extends ListActivity implements OnItemClickListener {
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RestClient.setAuth(this);
        
    	m_extras = getIntent().getExtras();
    	m_isprivate = false;
    	m_group_ids = new TreeMap<String,String>();
    	m_group_sizes = new TreeMap<String,String>();
    	m_nsid = "";
    	
    	if (m_extras.containsKey("nsid")) {
    		m_nsid = m_extras.getString("nsid");
    	}
    	
		SharedPreferences auth_prefs = getSharedPreferences("Auth",0);
		if (auth_prefs.contains("nsid") && m_nsid.equals(auth_prefs.getString("nsid", "")) && !m_nsid.equals("")) {
	    		m_isprivate = true;
		}
		
		if (m_extras.containsKey("grouplist") && m_extras.getString("grouplist") != null) {
			FillGroupMap();
			FillListView();
		}
		else {
			finish();
		}
	}
	
	private void FillGroupMap() {
		String group_name = "";
		String group_id = "";

		try {
			if (m_extras.containsKey("grouplist")) {
				JSONArray grouplist = new JSONArray(m_extras.getString("grouplist"));

				if (grouplist != null) {
					JSONObject group_obj = null;
					for (int i = 0; i < grouplist.length(); i++) {
						group_obj = grouplist.getJSONObject(i);
						if (group_obj != null && group_obj.has("nsid")) {
							group_name = group_obj.getString("name");
							group_id = group_obj.getString("nsid");
							
							if (group_obj.has("photos")) {
								m_group_sizes.put(group_name, group_obj.getString("photos"));
							}
							else {
								m_group_sizes.put(group_name, "");
							}
							m_group_ids.put(group_name, group_id);
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void FillListView() {
		List < Map<String,String> > setList = new ArrayList < Map<String,String> >();
		for (String key : m_group_ids.keySet()) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("groupname", key);
			if (m_group_sizes.get(key).equals("")) {
				m.put("nphotos", "");
			}
			else {
				m.put("nphotos", m_group_sizes.containsKey(key) ? m_group_sizes.get(key) + " "
					  + getResources().getString(R.string.lblnphotos) : "");
			}
			setList.add(m);
		}
		
        setListAdapter(new SimpleAdapter(
							this,
							setList,
							R.layout.groups_list_item,
							new String[]{"groupname","nphotos"},
							new int[]{R.id.GroupTitle, R.id.GroupNPhotos}));
        getListView().setTextFilterEnabled(true);
        getListView().setOnItemClickListener(this);
	}
	
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		String groupname = ((TextView)view.findViewById(R.id.GroupTitle)).getText().toString(); 
		if (m_group_ids.containsKey(groupname)) {
			Intent i = new Intent(this, ImageGrid.class);
			i.putExtra("group_id", m_group_ids.get(groupname));
			i.putExtra("type", "pool");
			i.putExtra("title", groupname);
			i.putExtra("isprivate", m_isprivate);
			try {
				startActivity(i);
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	TreeMap<String,String> m_group_ids;
	TreeMap<String,String> m_group_sizes;
	Bundle m_extras;
	String m_nsid;
	boolean m_isprivate;
}
