package com.zmosoft.flickrfree;

import java.util.ArrayList;
import java.util.HashMap;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ImageCollections extends ListActivity implements OnItemClickListener {
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RestClient.setAuth(this);
        
    	m_extras = getIntent().getExtras();
    	m_isprivate = false;
    	m_collection_ids = new TreeMap<String,String>();
    	m_collection_sets = new TreeMap<String,JSONArray>();
    	m_nsid = "";
    	
    	// Get the nsid for the current set.
    	if (m_extras.containsKey("nsid")) {
    		m_nsid = m_extras.getString("nsid");
    	}
    	// Compare the set's nsid to the app user's nsid. If they are the same, then all
    	// calls to this set will be authenticated.
		SharedPreferences auth_prefs = getSharedPreferences("Auth",0);
		if (auth_prefs.contains("nsid") && m_nsid.equals(auth_prefs.getString("nsid", "")) && !m_nsid.equals("")) {
	    		m_isprivate = true;
		}
		try {
			FillCollectionMap();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		FillListView();
	}
	
	private void FillCollectionMap() throws JSONException {
		JSONObject json_obj = APICalls.collectionsGetTree(m_nsid);
		if (json_obj.has("collections") && json_obj.getJSONObject("collections").has("collection")) {
				JSONArray collectionslist = json_obj.getJSONObject("collections").getJSONArray("collection");
				JSONArray setslist;
				JSONObject collection_obj;
				for (int i = 0; i < collectionslist.length(); i++) {
					setslist = null;
					collection_obj = collectionslist.getJSONObject(i);
					if (collection_obj.has("title") && collection_obj.has("id")) {
						m_collection_ids.put(collection_obj.getString("title"), collection_obj.getString("id"));
						setslist = collection_obj.getJSONArray("set");
						if (setslist != null) {
							m_collection_sets.put(collection_obj.getString("title"), setslist);
						}
					}
				}
		}
	}
	
	private void FillListView() {
		m_collectionlist = new ArrayList < Map<String,String> >();
		Map<String, String> m;
		for (String key : m_collection_ids.keySet()) {
			m = new HashMap<String, String>();
			m.put("collection_name", key);
			Integer size = m_collection_sets.containsKey(key) ? m_collection_sets.get(key).length() : 0;
			String nSets_str = "";
			if (size > 0) {
				nSets_str = size.toString() + " Set";
				if (size > 1) {
					nSets_str += "s";
				}
			}
			m.put("extra_info", nSets_str);
			m_collectionlist.add(m);
		}
		
		ListView lv = getListView();
        lv.setAdapter(new SimpleAdapter(
							this,
							m_collectionlist,
							R.layout.collections_list_item,
							new String[]{"collection_name","extra_info"},
							new int[]{R.id.CollectionName, R.id.ExtraInfo}));
       	lv.setTextFilterEnabled(true);
       	lv.setOnItemClickListener(this);
	}
	
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		String collection_name = ((TextView)view.findViewById(R.id.CollectionName)).getText().toString();
		if (collection_name != null && m_collection_ids.containsKey(collection_name)) {
			String photoset_id = m_collection_ids.get(collection_name);
			JSONArray setlist = m_collection_sets.get(collection_name);
			if (setlist != null && photoset_id != null) {
				Intent i = new Intent(this, ImageSets.class);
				i.putExtra("photoset_id", photoset_id);
				i.putExtra("type", "by_setlist");
				i.putExtra("setlist", setlist.toString());
				i.putExtra("isprivate", m_isprivate);
				try {
					startActivity(i);
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	TreeMap<String,String> m_collection_ids;
	TreeMap<String,JSONArray> m_collection_sets;
	ArrayList< Map<String,String> > m_collectionlist;
	Bundle m_extras;
	String m_nsid;
	boolean m_isprivate;
}
