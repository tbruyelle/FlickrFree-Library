package com.zmosoft.flickrfree;

import java.io.IOException;
import java.util.TreeMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.zmosoft.flickrfree.GlobalResources.ImgSize;

public class ImageFullScreen extends Activity {

	private class GetImageInfoTask extends AsyncTask<Object, Object, Object> {
		
		@Override
		protected Object doInBackground(Object... params) {
	    	m_imginfo = APICalls.photosGetInfo(m_extras.getString("photo_id"));
	    	m_exif =  APICalls.photosGetExif(m_extras.getString("photo_id"));
    		m_imgcontexts = APICalls.photosGetAllContexts(m_extras.getString("photo_id"));
	    	try {
	    		m_tags = GetTags();
				m_imgsizes = GetImgSizes();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			m_isfavorite = (JSONParser.getInt(m_imginfo, "photo/isfavorite") == 1);
			
			return null;
		}
		
		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected void onPostExecute(Object result) {
			try {
				ShowImage();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	    private String GetTags() throws JSONException {
			String tags_str = "";
			JSONArray tags = JSONParser.getArray(m_imginfo, "photo/tags/tag");
			if (tags != null) {
				for (int i = 0; i < tags.length(); i++) {
					JSONObject tag_obj = tags.getJSONObject(i);
					String new_tag = JSONParser.getString(tag_obj, "_content");
					tags_str += ((new_tag != null) ? new_tag : "")
							  + ((i < tags.length() - 1) ? " " : "");
				}
			}
	    	return tags_str;
	    }
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_CONTEXT_MENU);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.imgfullscreen);
        
        RestClient.setAuth(this);
        
    	m_extras = getIntent().getExtras();
    	setProgressBarIndeterminateVisibility(true);
    	
    	if (savedInstanceState == null) {
			m_downloadSize = ImgSize.MED;
    		m_isprivate = m_extras.containsKey("isprivate") ? m_extras.getBoolean("isprivate") : false;
    		new GetImageInfoTask().execute();
        }
        else {
    		m_isprivate = savedInstanceState.getBoolean("isprivate");
    		m_tags = savedInstanceState.getString("tags");
    		int dlsize = savedInstanceState.getInt("downloadsize");
    		if (dlsize == 0) {
    			m_downloadSize = ImgSize.SMALLSQUARE;
    		}
    		else if (dlsize == 1) {
    			m_downloadSize = ImgSize.THUMB;
    		}
    		else if (dlsize == 2) {
    			m_downloadSize = ImgSize.SMALL;
    		}
    		else if (dlsize == 3) {
    			m_downloadSize = ImgSize.MED;
    		}
    		else if (dlsize == 4) {
    			m_downloadSize = ImgSize.LARGE;
    		}
    		else if (dlsize == 5) {
    			m_downloadSize = ImgSize.ORIG;
    		}
	    	try {
		    	m_imginfo = new JSONObject(savedInstanceState.getString("imginfo"));
		    	m_exif = new JSONObject(savedInstanceState.getString("exif"));
		    	m_imgcontexts = new JSONObject(savedInstanceState.getString("imgcontexts"));
				m_imgsizes = GetImgSizes();
				ShowImage();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	outState.putBoolean("isprivate", m_isprivate);
    	outState.putInt("downloadsize", m_downloadSize.getNum());
    	outState.putString("imginfo", m_imginfo != null ? m_imginfo.toString() : "");
    	outState.putString("exif", m_exif != null ? m_exif.toString() : "");
    	outState.putString("imgcontexts", m_imgcontexts != null ? m_imgcontexts.toString() : "");
    	outState.putString("tags", m_tags);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_full_screen_options_menu, menu);
        MenuItem fav_item = menu.getItem(2);

        fav_item.setTitle(m_isfavorite ? getResources().getString(R.string.mnu_unfavorite)
        							   : getResources().getString(R.string.mnu_favorite));
		if (m_imgcontexts == null || (!m_imgcontexts.has("set") && !m_imgcontexts.has("pool"))) {
			menu.removeItem(R.id.item_context);
		}

		return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i = null;
    	
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.item_info:
			i = new Intent(this, ImageInfo.class);
			if (i != null) {
				i.putExtra("photo_id",m_extras.getString("photo_id"));
				i.putExtra("isprivate", m_isprivate);
				i.putExtra("imginfo", m_imginfo != null ? m_imginfo.toString() : "");
				i.putExtra("exif", m_exif != null ? m_exif.toString() : "");
				startActivity(i);
			}
            return true;
        case R.id.item_comments:
			i = new Intent(this, ImageComments.class);
			if (i != null) {
				i.putExtra("photo_id",m_extras.getString("photo_id"));
				i.putExtra("imginfo", m_imginfo != null ? m_imginfo.toString() : "");
				startActivity(i);
			}
            return true;
        case R.id.item_favorite:
			if (m_extras.containsKey("photo_id")) {
				if (m_isfavorite) {
					APICalls.favoritesRemove(m_extras.getString("photo_id"));
					Toast.makeText(this, R.string.removed_favorite, Toast.LENGTH_SHORT).show();
					m_isfavorite = false;
			        item.setTitle(getResources().getString(R.string.mnu_favorite));
				}
				else {
					APICalls.favoritesAdd(m_extras.getString("photo_id"));
					Toast.makeText(this, R.string.added_favorite, Toast.LENGTH_SHORT).show();
					m_isfavorite = true;
			        item.setTitle(getResources().getString(R.string.mnu_unfavorite));
				}
				m_imginfo = APICalls.photosGetInfo(m_extras.getString("photo_id"));
			}
        	return true;
        case R.id.item_tags:
			i = new Intent(this, ImageTags.class);
			if (i != null) {
				i.putExtra("tags", m_tags);
				String nsid = JSONParser.getString(m_imginfo, "photo/owner/nsid");
				i.putExtra("nsid", (nsid != null) ? nsid : "");
				startActivity(i);
			}
        	return true;
        case R.id.item_download:
			showDialog(DIALOG_DOWNLOAD_IMG_SIZE);
			return true;
        case R.id.item_context:
			i = new Intent(this, ImageContext.class);
			i.putExtra("photo_id",m_extras.getString("photo_id"));
			i.putExtra("isprivate", m_isprivate);
			i.putExtra("contexts", m_imgcontexts != null ? m_imgcontexts.toString() : "");
			startActivity(i);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog = null;
    	
    	switch(id) {
    	case DIALOG_DOWNLOAD_IMG_SIZE:
			CharSequence[] size_names = GetImageSizeNames();
			
    		AlertDialog.Builder dbuilder = new AlertDialog.Builder(this)
    			.setTitle(R.string.dlg_imgsize_title)
    			.setIcon(android.R.drawable.ic_dialog_info)
    			.setSingleChoiceItems(size_names, ImgSize.MED.getNum(),
    				new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int choice) {
							ImgSize size_chosen = ImgSize.MED;
							if (choice == 0) {
								size_chosen = ImgSize.SMALL;
							}
							else if (choice == 1) {
								size_chosen = ImgSize.MED;
							}
							else if (choice == 2) {
								size_chosen = ImgSize.ORIG;
							}
							m_downloadSize = size_chosen;
						}
    				})
		        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int id) {
						initiateDownload(m_imgsizes.get(m_downloadSize));
						dialog.dismiss();
					}
		        })
		        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
		        });
			dialog = dbuilder.create();
			break;
    	}
    	return dialog;
    }
    
    private void initiateDownload(String url) {
    	if (url == null) {
			Toast.makeText(this, R.string.picture_retrieve_error, Toast.LENGTH_LONG).show();
    	}
    	else {
			Intent downloader_intent = new Intent(this, TransferService.class);
			downloader_intent.putExtra("url", url);
			downloader_intent.putExtra("type", "download");
			downloader_intent.putExtra("title", url.substring(url.lastIndexOf("/") + 1));
	
			// Start the downloader service and pass in the intent containing
			// the upload information.
			startService(downloader_intent);
			
			Toast.makeText(this, R.string.downloadstarting, Toast.LENGTH_SHORT).show();
    	}
    }
    
    private TreeMap<ImgSize, String> GetImgSizes() throws JSONException {
    	TreeMap<ImgSize, String> imgsizesmap = new TreeMap<ImgSize, String>();
    	if (m_extras.containsKey("photo_id")) {
        	// Get the list of available image sizes for this photo.
	        
			imgsizesmap = new TreeMap<ImgSize, String>();
			JSONObject imgsizes_obj = APICalls.photosGetSizes(m_extras.getString("photo_id"));
			JSONArray imgsizes = JSONParser.getArray(imgsizes_obj, "sizes/size");
			if (imgsizes != null) {
				// Iterate through the Image Sizes array and fill the imgsizesmap hash map.
				for (int i = 0; i < imgsizes.length(); i++) {
					JSONObject imgsize = imgsizes.getJSONObject(i);
					String label = JSONParser.getString(imgsize, "label");
					String source = JSONParser.getString(imgsize, "source");
					if (label != null && source != null) {
						if (label.equals("Square")) {
							imgsizesmap.put(ImgSize.SMALLSQUARE, source);
						}
						else if (label.equals("Thumbnail")) {
							imgsizesmap.put(ImgSize.THUMB, source);
						}
						else if (label.equals("Small")) {
							imgsizesmap.put(ImgSize.SMALL, source);
						}
						else if (label.equals("Medium")) {
							imgsizesmap.put(ImgSize.MED, source);
						}
						else if (label.equals("Original")) {
							imgsizesmap.put(ImgSize.ORIG, source);
						}
					}
				}
			}
    	}
    	return imgsizesmap;
    }
    
    private void ShowImage() throws IOException {
    	String img_url = "";

    	if (m_imgsizes.containsKey(ImgSize.MED)) {
    		img_url = m_imgsizes.get(ImgSize.MED);
    	}
    	else if (m_imgsizes.containsKey(ImgSize.SMALL)) {
    		img_url = m_imgsizes.get(ImgSize.SMALL);
    	}
    	
    	if (img_url != "") {
    		setProgressBarVisibility(true);
    		setProgressBarIndeterminateVisibility(true);
    		setProgress(Window.PROGRESS_START);
    		new GetCachedImageTask().execute(this, (ImageView)findViewById(R.id.imgview), img_url, true);
    	}
    	else {
    		// TODO If the image information doesn't exist, it can't load the image, so
    		// this kicks the user back to the previous page. This is an improvement over
    		// what it did before, which was to try to load the image forever. However,
    		// I should probably have it display some sort of error message so the user
    		// knows that something went wrong.
    		this.finish();
    	}

    	String title = JSONParser.getString(m_imginfo, "photo/title/_content");
		setTitle("\t" + ((title != null) ? title : ""));
    }

    private CharSequence[] GetImageSizeNames() {
    	CharSequence[] size_names_array = {};
		Vector<CharSequence> size_names = new Vector<CharSequence>();
		if (m_imgsizes == null) m_imgsizes = new TreeMap<ImgSize, String>();
		for (ImgSize key : m_imgsizes.keySet()) {
			if (key != ImgSize.SMALLSQUARE && key != ImgSize.THUMB) {
				size_names.add(key.toString());
			}
		}
		size_names_array = size_names.toArray(size_names_array);
		return size_names_array;
    }
    
	Bundle m_extras;
	String m_tags;
	TreeMap<ImgSize, String> m_imgsizes;
	ImgSize m_downloadSize;
	JSONObject m_imginfo;
	JSONObject m_exif;
	JSONObject m_imgcontexts;
	JSONObject m_comment_list;
	boolean m_isprivate;
	boolean m_isfavorite;
	
	static final int MENU_IMGINFO = 0;
	static final int MENU_IMGCOMMENTS = 1;
	static final int MENU_IMGCONTEXT = 2;
	static final int MENU_IMGTAGS = 3;
	static final int MENU_DOWNLOAD = 4;
	static final int MENU_SETFAVE = 5;
	
	static final int DIALOG_DOWNLOAD_IMG_SIZE = 6;
}
