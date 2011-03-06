package com.zmosoft.flickrfree;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ImageGrid extends Activity implements OnItemClickListener, OnClickListener {
	
	private class GetThumbnailsTask extends AsyncTask<Activity, Object, Object> {

		@Override
		protected Object doInBackground(Activity... params) {
			if (params.length < 1) {
				return null;
			}
			Activity activity = (Activity)params[0];
			m_adapter = (ImageAdapter)((GridView)activity.findViewById(R.id.gridview)).getAdapter();
	        if (m_imglist != null && m_imglist.length() <= GlobalResources.IMGS_PER_PAGE) {
	        	m_size = m_imglist.length();
	        }
	        else {
	        	m_size = GlobalResources.IMGS_PER_PAGE;
	        }
			String img_url;
			for (int i = 0; i < m_size && !isCancelled(); i++) {
				JSONObject img_obj;
				try {
					img_obj = m_imglist.getJSONObject(i);
					img_url = GlobalResources.getImageURL(img_obj.getString("farm"),
							 							  img_obj.getString("server"),
							 							  img_obj.getString("id"),
							 							  img_obj.getString("secret"),
							 							  GlobalResources.ImgSize.SMALLSQUARE, "jpg");
					GlobalResources.CacheImage(img_url, activity, false);
					publishProgress(img_url, i);
					GlobalResources.sleep(100);
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
		@Override
		protected void onPreExecute() {
	    	setProgressBarVisibility(true);
	    	setProgressBarIndeterminateVisibility(true);
	    	setProgress(0);
		}
		
		@Override
		protected void onPostExecute(Object result) {
			setProgress(Window.PROGRESS_END);
	    	setProgressBarVisibility(false);
	    	setProgressBarIndeterminateVisibility(false);
		}

		protected void onProgressUpdate(Object... progress) {
	    	if (progress.length > 1 && progress[0]instanceof String
	    		&& progress[1] instanceof Integer) {
	    		int i = (Integer)progress[1];
	    		m_adapter.setImgUrl(i, (String)progress[0]);
	    		m_adapter.notifyDataSetChanged();
		    	setProgress((int)((double)Window.PROGRESS_END * (double)i / (double)m_size));
	    	}
	    }

	    ImageAdapter m_adapter;
	    int m_size;
	}
	
	private class GetImageGridInfoTask extends AsyncTask<Boolean, Object, Object> {
		
		@Override
		protected Object doInBackground(Boolean... params) {
			boolean newload = params.length > 0 ? params[0] : m_newload;
			
	    	try {
	    		GetImageList(GlobalResources.IMGS_PER_PAGE, m_currentPage, newload);
	    	}
	    	catch (JSONException e) {
	    		e.printStackTrace();
	    	}
			
    		return null;
		}
		
		@Override
		protected void onPreExecute() {
	    	setProgressBarVisibility(true);
	    	setProgressBarIndeterminateVisibility(true);
		}
		
		@Override
		protected void onPostExecute(Object result) {
	    	UpdateGrid(m_currentPage);
	    	if (m_fail_msg != null) {
	    		showDialog(DIALOG_ERR);
	    	}
	    	setProgressBarVisibility(false);
	    	setProgressBarIndeterminateVisibility(false);
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.imagegrid);
    	((GridView)findViewById(R.id.gridview)).setOnItemClickListener(this);
        ((ImageButton)findViewById(R.id.BtnPrevPage)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.BtnNextPage)).setOnClickListener(this);
        ((Button)findViewById(R.id.BtnPageNum)).setOnClickListener(this);
        ((Button)findViewById(R.id.BtnPageChangeOK)).setOnClickListener(this);
        ((Button)findViewById(R.id.BtnPageChangeCancel)).setOnClickListener(this);
        
        RestClient.setAuth(this);
        
        refresh(savedInstanceState);
    }
    
    private void refresh(Bundle savedInstanceState) {
    	m_extras = getIntent().getExtras();
		m_currentPage = 1;
    	m_numPages = 0;
    	m_getthumbnailstask = null;
    	m_imglist = new JSONArray();
    	m_newload = savedInstanceState == null;
    	if (!m_newload) {
    		if (savedInstanceState.containsKey("currentpage")) {
    			m_currentPage = savedInstanceState.getInt("currentpage");
    		}
    		if (savedInstanceState.containsKey("numPages")) {
    			m_numPages = savedInstanceState.getInt("numPages");
    		}
    		if (savedInstanceState.containsKey("title")) {
    			m_title = savedInstanceState.getString("title");
    		}
    		if (savedInstanceState.containsKey("imglist")) {
    			try {
					m_imglist = new JSONArray(savedInstanceState.getString("imglist"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
    		}
    	}

        setHeaderMode(HEADER_MODE_PAGER);
        new GetImageGridInfoTask().execute(m_newload);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	outState.putInt("currentpage", m_currentPage);
    	outState.putInt("numPages", m_numPages);
    	outState.putString("imglist",m_imglist.toString());
    	outState.putString("title",m_title);
    }
    
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, MENU_REFRESH, 0, "Refresh");
	    return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case MENU_REFRESH:
	    	refresh(null);
	        return true;
	    }
	    return false;
	}

    private void GetImageList(int perPage, int page, boolean newload) throws JSONException {
    	String displayname = "";
    	m_fail_msg = null;
    	int nPics = -1;
    	SharedPreferences auth_prefs = getSharedPreferences("Auth",0);
    	
    	if (newload) {
	    	if (m_extras.containsKey("type")) {
		    	String[] paramNames = null, paramVals = null;
				JSONObject json_obj = null;
				String methodname = "";
				String obj_toplevel_key = "photos";
				
				Vector<String> pNames = new Vector<String>();
				Vector<String> pVals = new Vector<String>();
				
				pNames.add("per_page");
				pVals.add(String.valueOf(perPage));
				pNames.add("page");
				pVals.add(String.valueOf(page));
	
	    		if (m_extras.containsKey("list_obj")) {
	    			json_obj = new JSONObject(m_extras.getString("list_obj"));
	    			m_extras.remove("list_obj");
	    			m_title = m_extras.getString("title");
	    		}
	    		else {
					if (m_extras.getString("type").equals("favorites")) {
						pNames.add("user_id");
						pVals.add(m_extras.getString("nsid"));
		    	        methodname = GlobalResources.isAppUser(this, m_extras.getString("nsid"))
		    	                   ? "flickr.favorites.getList" : "flickr.favorites.getPublicList";
						displayname = GlobalResources.isAppUser(this, m_extras.getString("nsid"))
						           ? auth_prefs.getString("displayname", "") : APICalls.getNameFromNSID(m_extras.getString("nsid"));
						m_title = getResources().getString(R.string.imggrid_favorites) + " " + displayname;
		    		}
		    		else if (m_extras.getString("type").equals("photostream")) {
						pNames.add("user_id");
						pVals.add(m_extras.getString("nsid"));
						methodname = "flickr.photos.search";
						displayname = GlobalResources.isAppUser(this, m_extras.getString("nsid"))
						            ? auth_prefs.getString("displayname", "") : APICalls.getNameFromNSID(m_extras.getString("nsid"));
						m_title = getResources().getString(R.string.imggrid_photostream) +  " " + displayname;
		    		}
		    		else if (m_extras.getString("type").equals("set")) {
						pNames.add("photoset_id");
						pVals.add(m_extras.getString("photoset_id"));
						methodname = "flickr.photosets.getPhotos";
						obj_toplevel_key = "photoset";
						m_title = getResources().getString(R.string.imggrid_set);
						if (m_extras.containsKey("title") && !m_extras.getString("title").equals("")) {
							m_title += " \"" + m_extras.getString("title") + "\"";
						}
		    		}
		    		else if (m_extras.getString("type").equals("pool")) {
						pNames.add("group_id");
						pVals.add(m_extras.getString("group_id"));
						methodname = "flickr.groups.pools.getPhotos";
						m_title = getResources().getString(R.string.imggrid_pool);
						if (m_extras.containsKey("title") && !m_extras.getString("title").equals("")) {
							m_title += " \"" + m_extras.getString("title") + "\"";
						}
		    		}
		    		else if (m_extras.getString("type").equals("photo_search")) {
						m_title = getResources().getString(R.string.imggrid_allphotos_search_results);
						methodname = "flickr.photos.search";
						
			    		if (m_extras.containsKey("text") && !m_extras.getString("text").equals("")) {
							pNames.add("text");
							pVals.add(m_extras.getString("text"));
			    		}
			    		if (m_extras.containsKey("user_id") && !m_extras.getString("user_id").equals("")) {
							pNames.add("user_id");
							pVals.add(m_extras.getString("user_id"));
			    		}
			    		if (m_extras.containsKey("tags") && !m_extras.getString("tags").equals("")) {
							pNames.add("tags");
							pVals.add(m_extras.getString("tags"));
			    		}
		    		}
	    		}
	    		
	    		if (!methodname.equals("")) {
		    		paramNames = paramVals = new String[]{};
		    		paramNames = pNames.toArray(paramNames);
		    		paramVals = pVals.toArray(paramVals);
					json_obj = RestClient.CallFunction(methodname,paramNames,paramVals);
	    		}
	    		
	    		if (json_obj != null) {
	    			String stat = JSONParser.getString(json_obj, "stat");
	    			if (stat == null || stat.equals("fail")) {
	    				m_fail_msg = JSONParser.getString(json_obj, "message");
	    				if (m_fail_msg == null) {
	    					m_fail_msg = "Unknown Error while reading pool";
	    				}
	    			}
	    			else {
		    			m_imglist = JSONParser.getArray(json_obj, obj_toplevel_key + "/photo");
		    			if (m_imglist == null) {
		    				m_imglist = new JSONArray();
		    			}
		    			nPics = JSONParser.getInt(json_obj, obj_toplevel_key + "/total");
	    			}
	    		}
	    	}

			if (nPics > 0) {
				double ratio = (double)nPics / (double)perPage;
				double ratio_floor = Math.floor(ratio); 
				m_numPages = (int)ratio_floor;
				if (ratio > ratio_floor) {
					m_numPages += 1;
				}
			}
	    	
    	}
    }
    
    private void UpdateGrid(int page) {
		((ImageButton)findViewById(R.id.BtnPrevPage)).setEnabled(page > 1);
		((ImageButton)findViewById(R.id.BtnNextPage)).setEnabled(page < m_numPages);
        ((TextView)findViewById(R.id.BtnPageNum)).setText("Page " + m_currentPage + " of " + m_numPages);
        ImageAdapter adapter = new ImageAdapter(this, m_imglist.length());
        ((GridView)findViewById(R.id.gridview)).setAdapter(adapter);
        m_getthumbnailstask = new GetThumbnailsTask();
        m_getthumbnailstask.execute(this);

    	setTitle(m_title);
    }
    
    protected Dialog onCreateDialog(int id) {
		Dialog err_dialog = null;
		
    	switch(id) {
    	case DIALOG_ERR:

    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(m_fail_msg)
		           .setNeutralButton("OK", new DialogInterface.OnClickListener() {
		                             public void onClick(DialogInterface dialog, int id) {
		                            	 m_fail_msg = null;
		                                 ImageGrid.this.finish();
		                             }
		            });
			err_dialog = builder.create();
			break;
    	}

		return err_dialog;
    }
    
	@Override
	public void onItemClick(AdapterView arg0, View arg1, int position, long id) {
		try {
			if (position < m_imglist.length()) {
				JSONObject img_obj = m_imglist.getJSONObject((int) position);
	   			Intent i = new Intent(this, ImageFullScreen.class);
	   			i.putExtra("photo_id", img_obj.getString("id"));
	   			i.putExtra("isprivate", GlobalResources.isAppUser(this, m_extras.getString("nsid")));
	   			if (m_getthumbnailstask != null && m_getthumbnailstask.getStatus() != AsyncTask.Status.FINISHED) {
	   				m_getthumbnailstask.cancel(true);
	   				while (!m_getthumbnailstask.isCancelled()) {
	   					GlobalResources.sleep(50);
	   				}
	   			}
	   			startActivity(i);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.BtnPrevPage) {
			if (m_currentPage > 1) {
				m_currentPage -= 1;
		        new GetImageGridInfoTask().execute(true);
			}
		}
		else if (v.getId() == R.id.BtnNextPage) {
			if (m_currentPage < m_numPages) {
				m_currentPage += 1;
		        new GetImageGridInfoTask().execute(true);
			}
		}
		else if (v.getId() == R.id.BtnPageNum) {
			setHeaderMode(HEADER_MODE_SETPAGE);
		}
		else if (v.getId() == R.id.BtnPageChangeOK) {
			EditText pagenum_entry = (EditText)findViewById(R.id.EditPageNumber);
			String page_entry = pagenum_entry.getText().toString();
			int pagenum = 1;
			boolean valid_entry = false;
			try {
				pagenum = Integer.parseInt(page_entry);
				valid_entry = pagenum > 0 && pagenum <= m_numPages;
			} catch (NumberFormatException e) {
				valid_entry = false;
			}
			
			if (valid_entry) {
				m_currentPage = pagenum;
				setHeaderMode(HEADER_MODE_PAGER);
		        new GetImageGridInfoTask().execute(true);
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
			else {
				pagenum_entry.getText().clear();
			}
		}
		else if (v.getId() == R.id.BtnPageChangeCancel) {
			setHeaderMode(HEADER_MODE_PAGER);
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
	}

	private void setHeaderMode(int mode) {
		((LinearLayout)findViewById(R.id.ImgGridPager))
						.setVisibility(mode == HEADER_MODE_PAGER ?
								View.VISIBLE
							  : View.INVISIBLE);
		((RelativeLayout)findViewById(R.id.ImgGridPageSet))
						.setVisibility(mode == HEADER_MODE_SETPAGE ?
								View.VISIBLE
							  : View.INVISIBLE);
		
		if (mode == HEADER_MODE_SETPAGE) {
			EditText pagenum_entry = (EditText)findViewById(R.id.EditPageNumber);
			if (pagenum_entry != null) {
				pagenum_entry.setText(String.valueOf(m_currentPage));
				pagenum_entry.setSelection(0, pagenum_entry.getText().length());
				pagenum_entry.requestFocus();
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(pagenum_entry, InputMethodManager.SHOW_FORCED);
			}
		}
	}
	
	JSONArray m_imglist;
	GetThumbnailsTask m_getthumbnailstask;
	Bundle m_extras;
	String m_title;
	String m_fail_msg;
	int m_currentPage;
	int m_numPages;
	boolean m_newload;
	
	static final int DIALOG_PAGE_NUM = 0;
	static final int HEADER_MODE_PAGER = 1;
	static final int HEADER_MODE_SETPAGE = 2;
    static final int MENU_REFRESH = 3;
    
    static final int DIALOG_ERR = 3;
}
