package com.zmosoft.flickrfree;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.BadTokenException;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class UserView extends Activity implements OnItemClickListener, OnItemSelectedListener, OnClickListener {

	private class GetExtraInfoTask extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... params) {
			// The "nsid" string contains the User ID of the user that this view represents.
			// Look in the "extras" bundle for that ID. If it doesn't exist, then nsid will
			// be an empty string, indicating no user.
			String nsid = m_extras.containsKey("nsid") ? m_extras.getString("nsid") : "";

			// Get the user's buddy icon and display it.
			String icon_url = GlobalResources.GetBuddyIcon(m_userinfo);
			
			try {
				if (icon_url != "" && GlobalResources.CacheImage(icon_url, m_activity, false)) {
					Bitmap buddyicon = GlobalResources.GetCachedImage(icon_url, m_activity);
					if (buddyicon != null) {
						publishProgress(buddyicon);
					}
				}
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			// Get the number of photos in the user's photostream and publish that
			// result to the listview.
			String nPhotos_str = "";
			if (m_userinfo != null) {
				int nPhotos = JSONParser.getInt(m_userinfo, "person/photos/count/_content");
				if (nPhotos > 0) {
					nPhotos_str = nPhotos + " Photo";
					if (nPhotos > 1) {
						nPhotos_str += "s";
					}
				}
				publishProgress(m_actionnames[ACTION_PHOTOSTREAM], nPhotos_str);
			}
			
			if (!nsid.equals("")) {
				// Get the number of sets in the user's account and publish that
				// result to the listview.
				String nSets_str = "";
				m_photosets = APICalls.photosetsGetList(nsid);
				if (m_photosets != null) {
					JSONArray sets_arr = JSONParser.getArray(m_photosets, "photosets/photoset");
					int nSets = sets_arr != null
					            ? sets_arr.length()
					            : 0;
					if (nSets > 0) {
						nSets_str = nSets + " Set";
						if (nSets > 1) {
							nSets_str += "s";
						}
					}
					publishProgress(m_actionnames[ACTION_SETS], nSets_str);
				}

				String nCollections_str = "";
				m_collections = APICalls.collectionsGetTree(nsid);
				if (m_collections != null) {
					JSONArray collections_list = JSONParser.getArray(m_collections, "collections/collection");
					int nCollections = collections_list != null
					                   ? collections_list.length()
					                   : 0;
					if (nCollections > 0) {
						nCollections_str = nCollections + " Collection";
						if (nCollections > 1) {
							nCollections_str += "s";
						}
					}
					publishProgress(m_actionnames[ACTION_COLLECTIONS], nCollections_str);
				}

				String nTags_str = "";
				int nTags = !m_tags.equals("") ? m_tags.split(" ").length : 0;
				if (nTags > 0) {
					nTags_str = nTags + " Tag";
					if (nTags > 1) {
						nTags_str += "s";
					}
				}
				publishProgress(m_actionnames[ACTION_TAGS], nTags_str);

				String nFavorites_str = "";
				m_favorites = RestClient.CallFunction(GlobalResources.isAppUser(m_activity, nsid) 
													  ? "flickr.favorites.getList"
													  : "flickr.favorites.getPublicList",
													  new String[]{"user_id"},
													  new String[]{nsid});
				if (m_favorites != null) {
					int nFavorites = JSONParser.getInt(m_favorites, "photos/total");
					if (nFavorites > 0) {
						nFavorites_str = nFavorites + " Favorite";
						if (nFavorites > 1) {
							nFavorites_str += "s";
						}
					}
					publishProgress(m_actionnames[ACTION_FAVORITES], nFavorites_str);
				}

				try {
					String nGroups_str = "";
					if (GlobalResources.isAppUser(m_activity, nsid)) {
						m_groups = APICalls.groupsPoolsGetGroups();
					}
					else {
						m_groups = APICalls.peopleGetPublicGroups(nsid);
					}
					if (m_groups != null) {
						int nGroups;
						if (m_groups.getJSONObject("groups").has("total")) {
							nGroups = JSONParser.getInt(m_groups, "groups/total");
						}
						else {
							JSONArray groups_arr = JSONParser.getArray(m_groups, "groups/group");
							nGroups = groups_arr != null
							          ? groups_arr.length()
							          : 0;
						}
						if (nGroups > 0) {
							nGroups_str = nGroups + " Group";
							if (nGroups > 1) {
								nGroups_str += "s";
							}
						}
						publishProgress(m_actionnames[ACTION_GROUPS], nGroups_str);
					}
				} catch (JSONException e) {
				}
			}

			return null;
		}
		
		@Override
		protected void onProgressUpdate (Object... values) {
			if (values.length > 1 && values[0] instanceof String && values[1] instanceof String) {
				String action = (String)values[0];
				String info = (String)values[1];
				
				ListView listview = (ListView)findViewById(R.id.UserListView);
				HashMap <String, String> m = new HashMap<String, String>();
				if (m_extrainfomap.containsKey(action)) {
					if (!info.equals("")) {
						m = new HashMap<String, String>();
						m.put("action_name", action);
						m.put("extra_info", info);
						m_extrainfolist.set(m_extrainfomap.get(action), m);
					}
					if (listview.getAdapter() instanceof SimpleAdapter) {
						((SimpleAdapter)listview.getAdapter()).notifyDataSetChanged();
					}
				}
			}
			if (values.length == 1 && values[0] instanceof Bitmap) {
				((ImageView)findViewById(R.id.BuddyIcon)).setImageBitmap((Bitmap)values[0]);	
			}
		}
		
		@Override
		protected void onPreExecute() {
	    	setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected void onPostExecute(Object result) {
	    	setProgressBarIndeterminateVisibility(false);
	    	finalizeLoad();
		}
	}
	
	// Issues an API call to Flickr to retrieve information about a
	// given user. That information is stored as a JSON object called
	// m_userinfo.
	private class GetUserInfoTask extends AsyncTask<Bundle, Void, Object> {
		
		@Override
		protected Object doInBackground(Bundle... params) {
			Bundle extras = params.length > 0 ? params[0] : null;
			
			if (extras == null) {
				return null;
			}

			try {
				CheckAuthentication(getSharedPreferences("Auth",0));
				String nsid = extras.getString("nsid");
				m_userinfo = APICalls.peopleGetInfo(nsid);
				m_tags = GetTags(nsid);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			ClearUserDisplay();
	    	setProgressBarIndeterminateVisibility(true);
		}
		
		@Override
		protected void onPostExecute(Object result) {
	    	setProgressBarIndeterminateVisibility(false);
	    	try {
				SetUserDisplay();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	    private String GetTags(String nsid) throws JSONException {
			String tags_str = "";
			JSONArray tag_arr = JSONParser.getArray(APICalls.tagsGetListUser(nsid), "who/tags/tag");
			for (int i = 0; tag_arr != null && i < tag_arr.length(); i++) {
				tags_str = tags_str + tag_arr.getJSONObject(i).getString("_content");
				if (i < tag_arr.length() - 1) {
					tags_str = tags_str + " ";
				}
			}
	    	return tags_str;
	    }
	}
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.userview);

    	((Button)findViewById(R.id.btnManageAccounts)).setVisibility(View.INVISIBLE);

		((ListView)findViewById(R.id.UserListView)).setOnItemClickListener(this);
		((Button)findViewById(R.id.btnManageAccounts)).setOnClickListener(this);
		((Button)findViewById(R.id.btnOK)).setOnClickListener(this);
		((Button)findViewById(R.id.btnCancel)).setOnClickListener(this);
		((Button)findViewById(R.id.btnRemoveAccount)).setOnClickListener(this);
//		((CheckBox)findViewById(R.id.CheckBoxFriend)).setOnClickListener(this);
//		((CheckBox)findViewById(R.id.CheckBoxFamily)).setOnClickListener(this);

    	RestClient.setAuth(this);

    	// If getExtras() returns null, then this is the root activity.
        // Create a new Bundle for m_extras, and see if we can find an
        // nsid to put in it.
    	m_extras = getIntent().getExtras();
    	if (m_extras == null) {
    		m_extras = new Bundle();
    		m_extras.putString("nsid", getSharedPreferences("Auth",0).getString("nsid", ""));
    	}

		refresh();
	}
	
	private void refresh() {
        m_extrainfotask = null;
        m_photosets = null;
        m_favorites = null;
        m_groups = null;
        
		m_actionnames = getResources().getStringArray(R.array.main_user_view_list);
    	
    	try {
    		m_accounts = GetActiveAccounts(this);
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}

		new GetUserInfoTask().execute(m_extras);
	}
	
	// This method takes the authentication token stored in memory and checks it against
	// the Flickr API.
	private boolean CheckAuthentication(SharedPreferences auth_prefs) throws JSONException {
        boolean auth_ok = false;
        
    	RestClient.m_fulltoken = auth_prefs.getString("full_token", "");
    	
        if (!RestClient.m_fulltoken.equals("")) {
        	// If there is a token, then check to make sure it is still valid. 
        	auth_ok = APICalls.authCheckToken();
        }

        // If the authentication failed, then the token is invalid, so clear it and set
        // the app to a logged-out state.
        if (!auth_ok) {
			AuthenticateActivity.LogOut(auth_prefs);
			m_extras.putString("nsid","");
		}
		
		return auth_ok;
	}

	private void ClearUserDisplay() {
		((TextView)findViewById(R.id.TextUsername)).setText("");
		((TextView)findViewById(R.id.TextLocation)).setText("");
		
		ListView lv = ((ListView)findViewById(R.id.UserListView));
		lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{}));
		lv.setTextFilterEnabled(true);
	}

	private void SetUserDisplay() throws JSONException {
		String nsid = "";
		String username = "";
		String location = "";
		JSONObject userinfo = new JSONObject();
		int array_resource = R.array.no_user_view_list;
		String stat = JSONParser.getString(m_userinfo, "stat");
		if (stat != null && stat.equals("ok")) {
			userinfo = JSONParser.getObject(m_userinfo, "person");
			nsid = userinfo.getString("nsid");
		}
		
    	if (nsid.equals("")) {
    		m_usertype = UsrType.NOUSER;
    	}
    	else {
    		m_usertype  = GlobalResources.isAppUser(this,nsid)
    					? UsrType.APPUSER
    					: UsrType.OTHERUSER;
    		username = JSONParser.getString(userinfo, "username/_content");
       		location = JSONParser.getString(userinfo, "location/_content");
    	}

    	// Only show the Manage Accounts button if this is the user page of
    	// the registered app user.
    	((Button)findViewById(R.id.btnManageAccounts)).setVisibility(m_usertype != UsrType.OTHERUSER? View.VISIBLE : View.INVISIBLE);

//		LinearLayout cl = (LinearLayout)findViewById(R.id.LayoutContact);
//		cl.setVisibility(View.GONE);
		if (m_usertype == UsrType.APPUSER) {
			//TODO: Set background color of R.id.TextUsernameLabel.
			array_resource = R.array.main_user_view_list;
    	}
    	else if (m_usertype == UsrType.OTHERUSER) {
   			array_resource = R.array.user_view_list;

//    		cl.setVisibility(View.VISIBLE);
//    		boolean contact = userinfo.has("contact")
//    						&& userinfo.getInt("contact") == 1;
//   			boolean friend = userinfo.has("friend")
//   							&& userinfo.getInt("friend") == 1;
//   			boolean family = userinfo.has("family")
//   							&& userinfo.getInt("family") == 1;
//   			((CheckBox)findViewById(R.id.CheckBoxContact)).setChecked(contact);
//   			if (contact) {
//   				((CheckBox)findViewById(R.id.CheckBoxFriend)).setEnabled(true);
//   				((CheckBox)findViewById(R.id.CheckBoxFamily)).setEnabled(true);
//	   			((CheckBox)findViewById(R.id.CheckBoxFriend)).setChecked(friend);
//	   			((CheckBox)findViewById(R.id.CheckBoxFamily)).setChecked(family);
//   			}
//   			else {
//   				((CheckBox)findViewById(R.id.CheckBoxFriend)).setEnabled(false);
//   				((CheckBox)findViewById(R.id.CheckBoxFamily)).setEnabled(false);
//   			}
    	}

		((TextView)findViewById(R.id.TextUsername)).setText(username);
		((TextView)findViewById(R.id.TextLocation)).setText(location);
		
		m_extrainfolist = new ArrayList < Map<String,String> >();
		m_extrainfomap = new HashMap <String, Integer>();
		String[] action_array = getResources().getStringArray(array_resource);
		Map<String, String> m;
		for (int i = 0; i < action_array.length; i++) {
			m = new HashMap<String, String>();
			m.put("action_name", action_array[i]);
			m.put("extra_info", "");
			m_extrainfolist.add(m);
			m_extrainfomap.put(action_array[i], i);
		}
		
		ListView lv = ((ListView)findViewById(R.id.UserListView));
        lv.setAdapter(new SimpleAdapter(
							this,
							m_extrainfolist,
							R.layout.userview_list_item,
							new String[]{"action_name","extra_info"},
							new int[]{R.id.ActionTitle, R.id.ExtraInfo}));
        m_extrainfotask = new GetExtraInfoTask();
        m_extrainfotask.execute();
	}

	private void finalizeLoad() {
//		try {
//	    	SharedPreferences user_prefs = getSharedPreferences("UserPrefs",0);
//	    	if (!user_prefs.getBoolean(GlobalResources.HAS_NOTIFIED_UPGRADE, false)) {
//	    		SharedPreferences.Editor user_prefs_editor = user_prefs.edit();
//	    		user_prefs_editor.putBoolean(GlobalResources.HAS_NOTIFIED_UPGRADE, true);
//	    		user_prefs_editor.commit();
//	    		showDialog(DIALOG_UPGRADE);
//	    	}
//		} catch (BadTokenException e) {
//			
//		}
	}
	
	static public TreeMap<String, JSONObject> GetActiveAccounts(Activity activity) throws JSONException {
		SharedPreferences auth_prefs = activity.getSharedPreferences("Auth",0);
		TreeMap<String, JSONObject> accounts = new TreeMap<String, JSONObject>();
		
		for (String key : auth_prefs.getAll().keySet()) {
			if (key.contains("FlickrUsername_") && key.indexOf("FlickrUsername_") == 0) {
				accounts.put(key.substring(15), new JSONObject(auth_prefs.getString(key, "")));
			}
		}
		return accounts;
	}
	
	private void FillAccountSpinner() {
		Spinner spnAccts = (Spinner)findViewById(R.id.spnChooseAccount);
		ArrayList<String> spnItems = new ArrayList<String>();
		String[] spnItemsArray = new String[]{};
		
		if (m_accounts != null) {
			spnItems.add(getResources().getString(R.string.newaccount));
			for (String key : m_accounts.keySet()) {
				spnItems.add(key);
			}
			spnItemsArray = spnItems.toArray(spnItemsArray);

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		            android.R.layout.simple_spinner_item, spnItemsArray);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spnAccts.setAdapter(adapter);
		}
	}
	
	private void SetActiveAccount(String username) {
		if (username.equals(getResources().getString(R.string.newaccount))) {
			Intent i = new Intent(this,AuthenticateActivity.class);
			startActivityForResult(i,GlobalResources.ADD_ACCOUNT_REQ);
		}
		else {
			AuthenticateActivity.SetActiveUser(getSharedPreferences("Auth",0), username);
		}
	}
	
	private void setSpinnerTo(String username) {
		Spinner spnAccounts = ((Spinner)findViewById(R.id.spnChooseAccount));
		int i;
		String name = "";
		for (i = 0; !name.equals(username) && i < spnAccounts.getAdapter().getCount(); ++i) {
			name = spnAccounts.getAdapter().getItem(i).toString();
			if (name.equals(username)) {
				spnAccounts.setSelection(i);
			}
		}
	}

	private String getSelectedName() {
		Spinner spnAccounts = ((Spinner)findViewById(R.id.spnChooseAccount));
		if (spnAccounts != null) {
			View sv = spnAccounts.getSelectedView();
			if (sv != null) {
				return ((TextView)sv).getText().toString();
			}
		}
		return "";
	}
	
	private void removeSelectedAccount() {
		String sel_acct = getSelectedName();
		if (!sel_acct.equals("")) {
			AuthenticateActivity.RemoveUser(getSharedPreferences("Auth",0), sel_acct);
		}
		((LinearLayout)findViewById(R.id.manageAccountsHeader)).setVisibility(View.GONE);
		((RelativeLayout)findViewById(R.id.accountHeader)).setVisibility(View.VISIBLE);
		refresh();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GlobalResources.MANAGE_ACCOUNTS_REQ) {
			m_extras.putString("nsid", getSharedPreferences("Auth",0).getString("nsid", ""));

	        m_photosets = null;
	        m_favorites = null;
	        ((ImageView)findViewById(R.id.BuddyIcon)).setImageBitmap(null);
        	new GetUserInfoTask().execute(m_extras);
		}
		else if (requestCode == GlobalResources.ADD_ACCOUNT_REQ) {
			((LinearLayout)findViewById(R.id.manageAccountsHeader)).setVisibility(View.GONE);
			((RelativeLayout)findViewById(R.id.accountHeader)).setVisibility(View.VISIBLE);

			if (resultCode == AuthenticateActivity.AUTH_SUCCESS) {
				try {
					m_accounts = GetActiveAccounts(this);
				} catch (JSONException e) {
					e.printStackTrace();
				}
		        m_photosets = null;
		        m_favorites = null;
				SetActiveAccount(getSharedPreferences("Auth",0).getString("username", ""));
	
				m_extras.putString("nsid", getSharedPreferences("Auth",0).getString("nsid", ""));
	
		        ((ImageView)findViewById(R.id.BuddyIcon)).setImageBitmap(null);
	        	new GetUserInfoTask().execute(m_extras);
			}
		}
		else if (requestCode == GlobalResources.PICK_IMAGE_REQ) {
			if (data != null) {
			    Uri uri = data.getData();
			    if (uri != null) {
				  Intent i = new Intent(this, PictureSettings.class);
				  i.putExtra("image_uri", uri.toString());
				  i.putExtra("action", "upload");
				  startActivity(i);
			    }
			}
		}
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
    	switch(id) {
    	case DIALOG_WARN_REMOVE_ACCOUNT:
    		dialog.setTitle("Account \"" + getSelectedName() + "\"");
    		break;
    	}
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AssetManager assetManager = null;
		InputStream stream = null;
		String dialog_text = null;
		
		AlertDialog.Builder builder;
    	switch(id) {
    	case DIALOG_WARN_REMOVE_ACCOUNT:
    		builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msgremoveaccount)
				   .setTitle("Account")
			       .setIcon(android.R.drawable.ic_dialog_alert)
		           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		                             public void onClick(DialogInterface dialog, int id) {
		                            	 removeSelectedAccount();
		                            	 dialog.dismiss();
		                             }
		            })
		           .setNegativeButton("No", new DialogInterface.OnClickListener() {
		                             public void onClick(DialogInterface dialog, int id) {
		                            	 dialog.dismiss();
		                             }
		            });
			dialog = builder.create();
			break;
    	case DIALOG_UPGRADE:
    		assetManager = getAssets();
    		stream = null;
    		dialog_text = "";
        	try {
        		stream = assetManager.open("upgrade_info.html");
    	        if (stream != null) {
    		        byte[] buffer;
    		        int result = 0;
    		        while (result >= 0) {
    		        	buffer = new byte[256];
    		        	result = stream.read(buffer);
    		        	dialog_text += new String(buffer);
    		        }
    	        }
        	    stream.close();

        		builder = new AlertDialog.Builder(this);
        		LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        	    View layout = inflater.inflate(R.layout.upgrade_dialog_layout, null);
        	    builder.setView(layout);
			    builder.setTitle(R.string.ttlupgrade)
			    	   .setIcon(android.R.drawable.ic_dialog_info);
        	    
        	    Button btn_ok = (Button)layout.findViewById(R.id.BtnUpgrade);
        	    btn_ok.setOnClickListener(new View.OnClickListener() {
    			                             public void onClick(View v) {
    			                            	 try {
	    		                            	     Intent intent = new Intent(Intent.ACTION_VIEW);
	    		                            	     intent.setData(Uri.parse("market://search?q=pname:com.zmosoft.flickrcompanion"));
	    		                            	     startActivity(intent);
    			                            	 } catch (ActivityNotFoundException e) {
    			                            		 //TODO: Display error dialog when this exception is caught. Something
    			                            		 // along the lines of "Cannot open Market".
    			                            	 }
    			                             }
        	    });
        	    Button btn_no = (Button)layout.findViewById(R.id.BtnNotNow);
        	    btn_no.setOnClickListener(new View.OnClickListener() {
    			                             public void onClick(View v) {
    			                            	 dismissDialog(DIALOG_UPGRADE);
    			                             }
        	    });

        	    WebView dialog_text_view = (WebView)layout.findViewById(R.id.UpgradeInfo);
        	    dialog_text_view.loadData(dialog_text, "text/html", "utf-8");
        	    dialog = builder.create();
	        } catch (IOException e) {
	        }

    		break;
    	}

		return dialog;
    }
    
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnManageAccounts) {
			((LinearLayout)findViewById(R.id.manageAccountsHeader)).setVisibility(View.VISIBLE);
			((RelativeLayout)findViewById(R.id.accountHeader)).setVisibility(View.GONE);
    		FillAccountSpinner();
			setSpinnerTo(getSharedPreferences("Auth",0).getString("username", ""));
		}
		else if (v.getId() == R.id.btnRemoveAccount) {
			showDialog(DIALOG_WARN_REMOVE_ACCOUNT);
		}
		else if (v.getId() == R.id.btnOK) {
			String sel_user = ((TextView)((Spinner)findViewById(R.id.spnChooseAccount)).getSelectedView()).getText().toString();
			SetActiveAccount(sel_user);
			((LinearLayout)findViewById(R.id.manageAccountsHeader)).setVisibility(View.GONE);
			((RelativeLayout)findViewById(R.id.accountHeader)).setVisibility(View.VISIBLE);
			
			m_extras.putString("nsid", getSharedPreferences("Auth",0).getString("nsid", ""));

	        m_photosets = null;
	        m_favorites = null;
	        ((ImageView)findViewById(R.id.BuddyIcon)).setImageBitmap(null);
        	new GetUserInfoTask().execute(m_extras);
		}
		else if (v.getId() == R.id.btnCancel) {
			((LinearLayout)findViewById(R.id.manageAccountsHeader)).setVisibility(View.GONE);
			((RelativeLayout)findViewById(R.id.accountHeader)).setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onItemSelected(AdapterView arg0, View arg1, int arg2, long arg3) {
		
	}

	@Override
	public void onNothingSelected(AdapterView arg0) {
	}

	@Override
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		String command = ((TextView)view.findViewById(R.id.ActionTitle)).getText().toString();
		if (command.equals(m_actionnames[ACTION_UPGRADE])) {
			showDialog(DIALOG_UPGRADE);
		}
		else if (command.equals(m_actionnames[ACTION_PHOTOSTREAM])) {
			Intent i = new Intent(this, ImageGrid.class);
			i.putExtra("type", "photostream");
			i.putExtra("nsid", m_extras.getString("nsid"));
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivity(i);
		}
		else if (command.equals(m_actionnames[ACTION_SETS])) {
			Intent i = new Intent(this, ImageSets.class);
			if (m_photosets == null) {
				i.putExtra("type","by_nsid");
				i.putExtra("nsid", m_extras.getString("nsid"));
			}
			else {
				i.putExtra("type","by_setlist");
				i.putExtra("setlist", JSONParser.getString(m_photosets, "photosets/photoset"));
			}
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivity(i);
		}
		else if (command.equals(m_actionnames[ACTION_COLLECTIONS])) {
			Intent i = new Intent(this, ImageCollections.class);
			i.putExtra("nsid", m_extras.getString("nsid"));
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivity(i);
		}
		else if (command.equals(m_actionnames[ACTION_TAGS])) {
			Intent i = new Intent(this, ImageTags.class);
			i.putExtra("nsid", m_extras.getString("nsid"));
			i.putExtra("tags", m_tags);
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivity(i);
		}
		else if (command.equals(m_actionnames[ACTION_FAVORITES])) {
			Intent i = new Intent(this, ImageGrid.class);
			try {
				i.putExtra("type", "favorites");
				i.putExtra("nsid", m_extras.getString("nsid"));
				if (m_favorites != null) {
					i.putExtra("title", "Favorites for \"" + APICalls.getNameFromNSID(m_extras.getString("nsid")) + "\"");
					i.putExtra("list_obj", m_favorites.toString());
				}
				if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
					m_extrainfotask.cancel(true);
					while (!m_extrainfotask.isCancelled()) {
						GlobalResources.sleep(50);
					}
				}
				startActivity(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else if (command.equals(m_actionnames[ACTION_GROUPS])) {
			Intent i = new Intent(this, Groups.class);
			i.putExtra("grouplist", JSONParser.getString(m_groups, "groups/group"));
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivity(i);
		}
		else if (command.equals(m_actionnames[ACTION_CONTACTS])) {
			Intent i = new Intent(this, ContactsView.class);
			i.putExtra("nsid", getSharedPreferences("Auth",0).getString("nsid", ""));
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivity(i);
		}
		else if (command.equals(m_actionnames[ACTION_SEARCH])) {
			Intent i = new Intent(this, SearchView.class);
			i.putExtra("nsid", getSharedPreferences("Auth",0).getString("nsid", ""));
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivity(i);
		}
		else if (command.equals(m_actionnames[ACTION_UPLOAD])) {
			try {
				startActivityForResult(new Intent(Intent.ACTION_PICK,
					  							   android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
		  							   GlobalResources.PICK_IMAGE_REQ);
			} catch (ActivityNotFoundException e) {
				// TODO: If this exception comes up, that means that the Media app is not working or has been
				// disabled by the user. Pop up an error dialog here explaining the problem so the user knows
				// that something is wrong with their system.
			}
		}
	}


	private enum UsrType {
    	APPUSER, OTHERUSER, NOUSER;
    }

    static final int ACTION_UPGRADE = 0;
    static final int ACTION_PHOTOSTREAM = 1;
    static final int ACTION_SETS = 2;
    static final int ACTION_COLLECTIONS = 3;
    static final int ACTION_TAGS = 4;
    static final int ACTION_FAVORITES = 5;
    static final int ACTION_GROUPS = 6;
    static final int ACTION_CONTACTS = 7;
    static final int ACTION_SEARCH = 8;
    static final int ACTION_UPLOAD = 9;
    
    static final int DIALOG_WARN_REMOVE_ACCOUNT = 11;
    static final int DIALOG_UPGRADE = 12;
    
	Bundle m_extras;
	Activity m_activity = this;
	UsrType m_usertype;
	HashMap <String, Integer> m_extrainfomap;
	List < Map<String,String> > m_extrainfolist;
    TreeMap<String, JSONObject> m_accounts;
    String m_currentAccount;
	GetExtraInfoTask m_extrainfotask;
	String[] m_actionnames;
	String m_tags;
	JSONObject m_userinfo;
	JSONObject m_photosets;
	JSONObject m_collections;
	JSONObject m_favorites;
	JSONObject m_groups;
}
