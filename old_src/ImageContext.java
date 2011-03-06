package com.zmosoft.flickrfree;

import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ImageContext extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagecontext);
        
        RestClient.setAuth(this);
        
        m_extras = getIntent().getExtras();
		m_isprivate = false;
    	if (m_extras.containsKey("isprivate")) {
    		m_isprivate = m_extras.getBoolean("isprivate");
    	}
    	((LinearLayout)findViewById(R.id.ImgContextSetsLayout)).setVisibility(View.GONE);
    	((LinearLayout)findViewById(R.id.ImgContextPoolsLayout)).setVisibility(View.GONE);

    	try {
    		if (m_extras.containsKey("contexts")) {
    			JSONObject contexts = new JSONObject(m_extras.getString("contexts"));
		    	FillSetsMap(contexts);
		        FillPoolsMap(contexts);
    		}
    		else {
    			finish();
    		}
		} catch (JSONException e) {
			e.printStackTrace();
		}
        DisplayContexts();
    }

    private void DisplayContexts() {
		LinearLayout lv;
		
		if (m_set_ids.size() > 0) {
	    	lv = (LinearLayout)findViewById(R.id.ImgContextSetsLayout);
	    	((LinearLayout)findViewById(R.id.ImgContextSetsLayout)).setVisibility(View.VISIBLE);
			RelativeLayout entry;
			for (String key : m_set_ids.keySet()) {
	    		entry = (RelativeLayout)View.inflate(this, R.layout.pools_list_item, null);
	    		((TextView)entry.findViewById(R.id.PoolTitle)).setText(key);
	    		((TextView)entry.findViewById(R.id.PoolNPhotos)).setText(
	    				(m_set_sizes.containsKey(key) && !m_set_sizes.get(key).equals("")
						? m_set_sizes.get(key) + " " + getResources().getString(R.string.lblnphotos)
								: ""));
	    		
				entry.setClickable(true);
				entry.setOnClickListener(this);
	    		lv.addView(entry);

			}
		}

		if (m_pool_ids.size() > 0) {
	    	lv = (LinearLayout)findViewById(R.id.ImgContextPoolsLayout);
	    	((LinearLayout)findViewById(R.id.ImgContextPoolsLayout)).setVisibility(View.VISIBLE);
			RelativeLayout entry;
			for (String key : m_pool_ids.keySet()) {
	    		entry = (RelativeLayout)View.inflate(this, R.layout.pools_list_item, null);
	    		((TextView)entry.findViewById(R.id.PoolTitle)).setText(key);
	    		((TextView)entry.findViewById(R.id.PoolNPhotos)).setText(
	    				(m_pool_sizes.containsKey(key) && !m_pool_sizes.get(key).equals("")
						? m_pool_sizes.get(key) + " " + getResources().getString(R.string.lblnphotos)
								: ""));
	    		
				entry.setClickable(true);
				entry.setOnClickListener(this);
	    		lv.addView(entry);

			}
		}
    }
    
    private void FillSetsMap(JSONObject imgcontexts) throws JSONException {
    	m_set_ids = new TreeMap<String,String>();
    	m_set_sizes = new TreeMap<String,String>();
    	if (imgcontexts.has("set") && !imgcontexts.getString("set").equals("")) {
			JSONArray sets = imgcontexts.getJSONArray("set");
			String photoset_id, nphotos;
			for (int i = 0; i < sets.length(); i++) {
				photoset_id = sets.getJSONObject(i).getString("id");
				nphotos = GetPhotosetSize(photoset_id);
				m_set_ids.put(sets.getJSONObject(i).getString("title"),
						   sets.getJSONObject(i).getString("id"));
				m_set_sizes.put(sets.getJSONObject(i).getString("title"),nphotos);
			}
    	}
    }
    
    private void FillPoolsMap(JSONObject imgcontexts) throws JSONException {
    	m_pool_ids = new TreeMap<String,String>();
    	m_pool_sizes = new TreeMap<String,String>();
    	if (imgcontexts.has("pool") && !imgcontexts.getString("pool").equals("")) {
			JSONArray pools = imgcontexts.getJSONArray("pool");
			for (int i = 0; i < pools.length(); i++) {
				m_pool_ids.put(pools.getJSONObject(i).getString("title"),
							pools.getJSONObject(i).getString("id"));
				m_pool_sizes.put(pools.getJSONObject(i).getString("title"),"");
			}
    	}
    }

    private String GetPhotosetSize(String id) throws JSONException {
    	JSONObject json_obj = APICalls.photosetsGetInfo(id);
    	return (json_obj.has("photoset") && json_obj.getJSONObject("photoset").has("photos"))
    			? json_obj.getJSONObject("photoset").getString("photos")
    			: "";
    }
    
	@Override
	public void onClick(View view) {
		String title = "";
		Intent i = new Intent(this,ImageGrid.class);
		if (view.getParent() == findViewById(R.id.ImgContextSetsLayout)) {
			title = ((TextView)view.findViewById(R.id.PoolTitle)).getText().toString();
			i.putExtra("photoset_id", m_set_ids.get(title));
			i.putExtra("type", "set");
			i.putExtra("isprivate", m_isprivate);
		}
		if (view.getParent() == findViewById(R.id.ImgContextPoolsLayout)) {
			title = ((TextView)view.findViewById(R.id.PoolTitle)).getText().toString();
			i.putExtra("group_id", m_pool_ids.get(title));
			i.putExtra("type", "pool");
		}
		i.putExtra("title", title);
		
		try {
			startActivity(i);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	Bundle m_extras;
    TreeMap<String,String> m_set_ids;
    TreeMap<String,String> m_set_sizes;
    TreeMap<String,String> m_pool_ids;
    TreeMap<String,String> m_pool_sizes;
    boolean m_isprivate;
}
