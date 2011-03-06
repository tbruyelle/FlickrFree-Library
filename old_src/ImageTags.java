package com.zmosoft.flickrfree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ImageTags extends Activity implements OnItemClickListener {
	
	private class TagSearchTask extends AsyncTask<Object, String, Object> {

		@SuppressWarnings("unchecked")
		@Override
		protected Object doInBackground(Object... params) {
			SortedSet<String> tags = (params.length > 0 && params[0] instanceof SortedSet) ? (SortedSet<String>)params[0] : null;
			n_tags = tags.size();
			n_completed = 0;
			JSONObject photolist = null;
			if (tags != null) {
				Iterator<String> itr = tags.iterator();
				String tag, nphotos_str;
				int i = 0, nphotos = 0;
				while (itr.hasNext() && !isCancelled()) {
					tag = itr.next();
					photolist = APICalls.photosSearch(m_nsid, null, tag);
					if (photolist != null) {
						m_tagmap.put(tag, photolist);
						n_completed += 1;
						try {
							nphotos = photolist.getJSONObject("photos").getInt("total");
							if (nphotos == 1) {
								nphotos_str = Integer.toString(nphotos) + " Photo";
							}
							else {
								nphotos_str = Integer.toString(nphotos) + " Photos";
							}
						} catch (JSONException e) {
							nphotos_str = "";
						}
						publishProgress(tag, nphotos_str);
					}
					i++;
					GlobalResources.sleep(100);
				}
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate (String... values) {
			if (values.length > 1 && !isCancelled()) {
				String tag = (String)values[0];
				String result = (String)values[1];
				
				ListView listview = (ListView)findViewById(R.id.ImageTagsList);
				if (!result.equals("") && m_taglistmap.containsKey(tag)) {
					HashMap<String, String> m = new HashMap<String, String>();
					m.put("tag_name", tag);
					m.put("extra_info", result);
					m_taglist.set(m_taglistmap.get(tag), m);
					((SimpleAdapter)listview.getAdapter()).notifyDataSetChanged();
				}
				setProgress((int)((double)Window.PROGRESS_END * (double)n_completed / (double)n_tags));
			}
		}
		
		@Override
		protected void onCancelled() {
	    	setProgressBarVisibility(false);
	    	setProgressBarIndeterminateVisibility(false);
		}
		
		@Override
		protected void onPreExecute() {
	    	setProgressBarVisibility(true);
	    	setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected void onPostExecute(Object result) {
	    	setProgressBarVisibility(false);
	    	setProgressBarIndeterminateVisibility(false);
		}
		
		int n_tags;
		int n_completed;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.imagetags);
        
    	m_tagsearchtask = null;
    	
    	RestClient.setAuth(this);
    	
        m_extras = getIntent().getExtras();
        m_tagmap = new HashMap <String, JSONObject>();
        if (m_extras != null) {
        	String tags = m_extras.containsKey("tags") ? m_extras.getString("tags") : "";
        	m_nsid = m_extras.containsKey("nsid") ? m_extras.getString("nsid") : "";
        	SortedSet<String> tagset = FillTagList(tags);
        	m_tagsearchtask = new TagSearchTask();
        	m_tagsearchtask.execute(tagset);
        }
    }
    
    private SortedSet<String> FillTagList(String tags) {
    	// Use a SortedSet to sort the tag values and put them
    	// in an array.
       	SortedSet<String> tagset = new TreeSet<String>();
       	String[] tag_arr = tags.split(" ");
       	for (int i = 0; i < tag_arr.length; i++) {
       		tagset.add(tag_arr[i]);
       	}
       	tag_arr = new String[]{};
       	tag_arr = tagset.toArray(tag_arr);

		m_taglist = new ArrayList < Map<String,String> >();
		m_taglistmap = new HashMap <String, Integer>();
		Map<String, String> m;
		for (int i = 0; i < tag_arr.length; i++) {
			m = new HashMap<String, String>();
			m.put("tag_name", tag_arr[i]);
			m.put("extra_info", "");
			m_taglist.add(m);
			m_taglistmap.put(tag_arr[i],i);
		}
		
		ListView lv = ((ListView)findViewById(R.id.ImageTagsList));
        lv.setAdapter(new SimpleAdapter(
							this,
							m_taglist,
							R.layout.tags_list_item,
							new String[]{"tag_name","extra_info"},
							new int[]{R.id.TagName, R.id.ExtraInfo}));
       	lv.setTextFilterEnabled(true);
       	lv.setOnItemClickListener(this);
       	
       	return tagset;
    }
    
	@Override
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		String tag = ((TextView)view.findViewById(R.id.TagName)).getText().toString();
		
		Intent i = new Intent(this,ImageGrid.class);
		i.putExtra("type", "photo_search");
		i.putExtra("tags", tag);
		i.putExtra("user_id", m_nsid);
		if (m_tagmap.containsKey(tag)) {
			i.putExtra("title", "Photos tagged with \"" + tag + "\"");
			i.putExtra("list_obj", m_tagmap.get(tag).toString());
		}
		if (m_tagsearchtask != null && m_tagsearchtask.getStatus() != AsyncTask.Status.FINISHED) {
			m_tagsearchtask.cancel(true);
			while (!m_tagsearchtask.isCancelled()) {
				GlobalResources.sleep(50);
			}
		}
		startActivity(i);
	}

	HashMap <String, JSONObject> m_tagmap;
	List < Map<String,String> > m_taglist;
	Map <String, Integer> m_taglistmap;
	String m_nsid;
	Bundle m_extras;
	TagSearchTask m_tagsearchtask;
}
