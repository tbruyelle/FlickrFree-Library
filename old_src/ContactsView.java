package com.zmosoft.flickrfree;

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
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ContactsView extends ListActivity implements OnItemClickListener {
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RestClient.setAuth(this);
        
    	m_extras = getIntent().getExtras();
    	m_isprivate = false;
    	m_contactsmap = new TreeMap<String,String>();
    	m_nsid = "";
    	
    	// Get the nsid for the current set.
    	if (m_extras.containsKey("nsid")) {
    		m_nsid = m_extras.getString("nsid");
    	}
    	// Compare the set's nsid to the app user's nsid. If they are the same, then all
    	// calls to this set will be authenticated.
		SharedPreferences auth_prefs = getSharedPreferences("Auth",0);
		m_isprivate = m_nsid.equals(auth_prefs.getString("nsid", "")) && !m_nsid.equals("");

		try {
			FillContactsMap();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		FillListView();
	}
	
	private void FillContactsMap() throws JSONException {
		String[] paramNames;
		String[] paramVals;
		String methodName;

		if (m_isprivate) {
			methodName = "flickr.contacts.getList";
			paramNames = null;
			paramVals = null;
		}
		else {
			methodName = "flickr.contacts.getPublicList";
			paramNames = new String[]{"user_id"};
			paramVals = new String[]{m_nsid};
		}
		JSONArray contactslist = JSONParser.getArray(RestClient.CallFunction(methodName,paramNames,paramVals),
													 "contacts/contact");
		JSONObject set_obj;
		for (int i = 0; contactslist != null && i < contactslist.length(); i++) {
			set_obj = contactslist.getJSONObject(i);
			String username = JSONParser.getString(set_obj, "username");
			String nsid = JSONParser.getString(set_obj, "nsid");
			if (username != null && nsid != null) {
				m_contactsmap.put(username, nsid);
			}
		}
	}
	
	private void FillListView() {
		String[] setNames = new String[m_contactsmap.size()];
		int i = 0;
		for (String key : m_contactsmap.keySet()) {
			setNames[i] = key;
			i++;
		}

		ArrayAdapter<String> a = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, setNames);
        setListAdapter(a);
        getListView().setTextFilterEnabled(true);
        getListView().setOnItemClickListener(this);
	}
	
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		if (m_contactsmap.containsKey(((TextView)view).getText())) {
			Intent i = new Intent(this, UserView.class);
			i.putExtra("nsid", m_contactsmap.get(((TextView)view).getText()));
			try {
				startActivity(i);
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	TreeMap<String,String> m_contactsmap;
	Bundle m_extras;
	String m_nsid;
	boolean m_isprivate;
}
