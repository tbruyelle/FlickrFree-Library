package com.zmosoft.flickrfree;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class SearchView extends Activity implements OnClickListener, OnItemSelectedListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.searchview);
        
        RestClient.setAuth(this);
        
        Spinner s = (Spinner)findViewById(R.id.SearchOptionsSpinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.search_options_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

        ((Spinner)findViewById(R.id.SearchOptionsSpinner)).setOnItemSelectedListener(this);
        ((Spinner)findViewById(R.id.SearchSubOptionsSpinner)).setOnItemSelectedListener(this);
        ((Button)findViewById(R.id.BtnSearch)).setOnClickListener(this);
        
        SetSearchOption(0);
    }
    
	private void SetSearchOption(long choice) {
		if (choice == PHOTO_SEARCH) {
			// Photo search
	        findViewById(R.id.SearchSubOptionsSpinner).setVisibility(View.VISIBLE);
	        Spinner s = (Spinner) findViewById(R.id.SearchSubOptionsSpinner);
	        ArrayAdapter adapter = ArrayAdapter.createFromResource(
	                this, R.array.search_photos_suboptions_list, android.R.layout.simple_spinner_item);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        s.setAdapter(adapter);
		}
		else if (choice == GROUP_SEARCH) {
			// Group search
	        findViewById(R.id.SearchSubOptionsSpinner).setVisibility(View.INVISIBLE);
		}
		else if (choice == TAG_SEARCH) {
			// Tag search
	        findViewById(R.id.SearchSubOptionsSpinner).setVisibility(View.INVISIBLE);
		}
		else if (choice == USER_SEARCH) {
			// Username search
	        findViewById(R.id.SearchSubOptionsSpinner).setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.BtnSearch) {
			Intent i;
			Spinner s = (Spinner)findViewById(R.id.SearchOptionsSpinner); 
			Spinner s2 = (Spinner)findViewById(R.id.SearchSubOptionsSpinner);
			String search_text;
			if (s.getSelectedItemId() == PHOTO_SEARCH) {
				// Search all photos
				search_text = (((EditText)findViewById(R.id.EditSearchText)).getText().toString());
				
				i = new Intent(this, ImageGrid.class);
				i.putExtra("type", "photo_search");
				i.putExtra("text", search_text);
				if (s2.getSelectedItemId() == 0) {
					// Search all photos
				}
				else if (s2.getSelectedItemId() == 1) {
					// Search user's photostream
					i.putExtra("user_id", "me");
				}
				else if (s2.getSelectedItemId() == 2) {
					// Search user's favorites
					
				}
				startActivity(i);
			}
			else if (s.getSelectedItemId() == GROUP_SEARCH) {
				// Search groups
				JSONObject groups_obj = APICalls.groupsSearch((((EditText)findViewById(R.id.EditSearchText)).getText().toString()), "20");
				i = new Intent(this, Groups.class);
				i.putExtra("grouplist", JSONParser.getString(groups_obj, "groups/group"));
				startActivity(i);
			}
			else if (s.getSelectedItemId() == TAG_SEARCH) {
				// Search tags
				search_text = (((EditText)findViewById(R.id.EditSearchText)).getText().toString());
				
				i = new Intent(this, ImageGrid.class);
				i.putExtra("type", "photo_search");
				i.putExtra("tags", search_text);
				startActivity(i);
			}
			else if (s.getSelectedItemId() == USER_SEARCH) {
				// Search by username
				String nsid = "";
				try {
					nsid = APICalls.getNSIDFromName(((EditText)findViewById(R.id.EditSearchText)).getText().toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if (nsid.equals("")) {
					// TODO: Pop up an Alert Dialog if username search fails.
				}
				else {
					i = new Intent(this, UserView.class);
					i.putExtra("nsid", nsid);
					startActivity(i);
				}
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView parent, View view, int position, long id) {
		if (parent != null) {
			if (parent.getId() == R.id.SearchOptionsSpinner) {
				SetSearchOption(id);
			}
			else if (parent.getId() == R.id.SearchSubOptionsSpinner) {
				
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView parent) {
	}
	
	private final static long PHOTO_SEARCH = 0;
	private final static long GROUP_SEARCH = 1;
	private final static long TAG_SEARCH = 2;
	private final static long USER_SEARCH = 3;
}
