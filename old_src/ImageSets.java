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

public class ImageSets extends ListActivity implements OnItemClickListener {
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RestClient.setAuth(this);
        
    	m_extras = getIntent().getExtras();
    	m_isprivate = false;
    	m_set_ids = new TreeMap<String,String>();
    	m_set_sizes = new TreeMap<String,String>();
    	m_nsid = "";
    	
    	if (m_extras.containsKey("nsid")) {
    		m_nsid = m_extras.getString("nsid");
    	}
    	
		SharedPreferences auth_prefs = getSharedPreferences("Auth",0);
		if (auth_prefs.contains("nsid") && m_nsid.equals(auth_prefs.getString("nsid", "")) && !m_nsid.equals("")) {
	    		m_isprivate = true;
		}
		FillSetMap();
		FillListView();
	}
	
	private void FillSetMap() {
		String set_title = "";
		String set_id = "";

		try {
			if (m_extras.containsKey("type")) {
				JSONObject json_obj = null;
				JSONArray setslist = null;
				if (m_extras.getString("type").equals("by_nsid")) {
					json_obj = APICalls.photosetsGetList(m_nsid);
					if (json_obj != null) {
						if (json_obj.has("photosets") && json_obj.getJSONObject("photosets").has("photoset")) {
							setslist = json_obj.getJSONObject("photosets").getJSONArray("photoset");
						}
					}
				}
				else if (m_extras.getString("type").equals("by_setlist")) {
					setslist = new JSONArray(m_extras.getString("setlist"));
				}
				
				if (setslist != null) {
					JSONObject set_obj = null, set_obj2 = null;
					for (int i = 0; i < setslist.length(); i++) {
						set_obj = setslist.getJSONObject(i);
						if (set_obj != null && set_obj.has("title") && set_obj.has("id")) {
							set_title = set_obj.getString("title");
							set_id = set_obj.getString("id");
							if (set_title.contains("_content")) {
								set_title = set_obj.getJSONObject("title").getString("_content");
								m_set_sizes.put(set_title, set_obj.getString("photos"));
							}
							else {
								set_obj2 = APICalls.photosetsGetInfo(set_id);
								if (set_obj2 != null) {
									m_set_sizes.put(set_title, set_obj2.getJSONObject("photoset").getString("photos"));
								}
							}
							m_set_ids.put(set_title, set_id);
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
		for (String key : m_set_ids.keySet()) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("setname", key);
			m.put("nphotos", m_set_sizes.containsKey(key) ? m_set_sizes.get(key) + " "
				  + getResources().getString(R.string.lblnphotos) : "");
			setList.add(m);
		}
		
        setListAdapter(new SimpleAdapter(
							this,
							setList,
							R.layout.sets_list_item,
							new String[]{"setname","nphotos"},
							new int[]{R.id.SetTitle, R.id.SetNPhotos}));
        getListView().setTextFilterEnabled(true);
        getListView().setOnItemClickListener(this);
	}
	
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		String setname = ((TextView)view.findViewById(R.id.SetTitle)).getText().toString(); 
		if (m_set_ids.containsKey(setname)) {
			Intent i = new Intent(this, ImageGrid.class);
			i.putExtra("photoset_id", m_set_ids.get(setname));
			i.putExtra("type", "set");
			i.putExtra("isprivate", m_isprivate);
			try {
				startActivity(i);
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	TreeMap<String,String> m_set_ids;
	TreeMap<String,String> m_set_sizes;
	Bundle m_extras;
	String m_nsid;
	boolean m_isprivate;
}
