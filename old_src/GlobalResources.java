package com.zmosoft.flickrfree;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;


public class GlobalResources {
    public static String m_EDITPERMS_URL = "http://www.flickr.com/services/auth/list.gne";
    
    public static final String INTENT_UPLOAD_STARTED = "com.zmosoft.flickrfree.UPLOAD_STARTED";
    public static final String INTENT_UPLOAD_FINISHED = "com.zmosoft.flickrfree.UPLOAD_FINISHED";
    public static final String INTENT_UPLOAD_FAILED = "com.zmosoft.flickrfree.UPLOAD_FAILED";
    public static final String INTENT_DOWNLOAD_STARTED = "com.zmosoft.flickrfree.DOWNLOAD_STARTED";
    public static final String INTENT_DOWNLOAD_FINISHED = "com.zmosoft.flickrfree.DOWNLOAD_FINISHED";
    public static final String INTENT_DOWNLOAD_FAILED = "com.zmosoft.flickrfree.DOWNLOAD_FAILED";
    public static final String INTENT_BIND_TRANSFER_SERVICE = "com.zmosoft.flickrfree.BIND_TRANSFER_SERVICE";
    public static final String INTENT_BIND_DOWNLOADER = "com.zmosoft.flickrfree.BIND_DOWNLOADER";
    public static final String INTENT_UPLOAD_PROGRESS_UPDATE = "com.zmosoft.flickrfree.UPLOAD_PROGRESS_UPDATE";
    public static final String INTENT_DOWNLOAD_PROGRESS_UPDATE = "com.zmosoft.flickrfree.DOWNLOAD_PROGRESS_UPDATE";
    public static final String INTENT_GET_PHOTOSTREAM = "com.zmosoft.flickrfree.GET_PHOTOSTREAM";
    public static final String INTENT_GET_POOL = "com.zmosoft.flickrfree.GET_POOL";
    public static final String INTENT_FLICKR_SEARCH = "com.zmosoft.flickrfree.FLICKR_SEARCH";
    public static final String INTENT_GET_FAVORITES = "com.zmosoft.flickrfree.GET_FAVORITES";
    public static final String INTENT_GET_USERLIST = "com.zmosoft.flickrfree.GET_USERLIST";
    public static final String INTENT_SET_USER = "com.zmosoft.flickrfree.SET_USER";
    
    public static final String HAS_NOTIFIED_UPGRADE = "has_notified_upgrade";
    
    public static final String TRANSFER_TYPE_UPLOAD = "Upload";
    public static final String TRANSFER_TYPE_DOWNLOAD = "Download";

    public static int API_DELAY_MS = 1000;
	public static int ERROR_DELAY_MS = 1000;

	static final int ADD_ACCOUNT_REQ = 1;
	static final int MANAGE_ACCOUNTS_REQ = 2;
	static final int PICK_IMAGE_REQ = 999;
	static final int IMGS_PER_PAGE = 20;
    static final int NRETRIES = 10;
	static final int UPLOADER_ID = 243;
	static final int DOWNLOADER_ID = 253;

	public enum ImgSize {
    	SMALLSQUARE(0), THUMB(1), SMALL(2), MED(3), LARGE(4), ORIG(5);
    	
    	private int m_sizenum;
    	
    	private ImgSize(int i) {
    		m_sizenum = i;
    	}
    	
    	public int getNum() {
    		return m_sizenum;
    	}

    	public void setNum(int num) {
    		m_sizenum = num;
    	}
    	
    	public String toString() {
    		if (m_sizenum == 0) {
    			return "Small Square";
    		}
    		else if (m_sizenum == 1) {
    			return "Thumb";
    		}
    		else if (m_sizenum == 2) {
    			return "Small";
    		}
    		else if (m_sizenum == 3) {
    			return "Medium";
    		}
    		else if (m_sizenum == 4) {
    			return "Large";
    		}
    		else if (m_sizenum == 5) {
    			return "Original";
    		}
    		else {
    			return "Unknown";
    		}
    	}
    }
    
	public static boolean CheckNetwork(Context context) {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo.State wifi_state = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		NetworkInfo.State mobile_state = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		
		if ((wifi_state == NetworkInfo.State.CONNECTED) || (mobile_state == NetworkInfo.State.CONNECTED)) {
			 return (!APICalls.ping().has("fail"));
		}
		else {
			return false;
		}
    }
	
    public static boolean isAppUser(Activity a, String nsid) {
    	return (nsid != "" && a.getSharedPreferences("Auth",0).getString("nsid", "").equals(nsid));
    }
    
    public static String getDisplayName(String username, String realname) {
    	String displayname = "";
    	
    	if (!realname.equals("")) {
    		displayname = realname + " (" + username + ")";
    	}
    	else {
    		displayname = username;
    	}
    	
    	return displayname;
    }
    
    public static Bitmap getBitmapFromURL(String url) throws JSONException, IOException {
        Bitmap bm = null;
        URL aURL = new URL(url);
        URLConnection conn = aURL.openConnection();
        conn.connect();
        InputStream is = conn.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        bm = BitmapFactory.decodeStream(bis);
        bis.close();
        is.close();

        return bm;
    }
    
    public static String getImageURL(String farm, String server, String id, String secret, ImgSize size, String extension) {
		String img_url = "http://farm" + farm + ".static.flickr.com/" + server
						+ "/" + id + "_" + secret;
		if (size == ImgSize.SMALLSQUARE) {
			img_url = img_url + "_s";
		}
		else if (size == ImgSize.THUMB) {
			img_url = img_url + "_t";
		}
		else if (size == ImgSize.SMALL) {
			img_url = img_url + "_m";			
		}
		else if (size == ImgSize.LARGE) {
			img_url = img_url + "_b";
		}
		else if (size == ImgSize.ORIG) {
			img_url = img_url + "_o";
		}
		img_url = img_url + "." + extension;
		
		return img_url;
    }

    public static String downloadImage(String url, String filename, boolean show_progress, Context context) throws IOException {
    	String dlpath = GetDownloadDir();
    	if (!dlpath.equals("")) {
    		return downloadImage(url, filename, dlpath, show_progress, context);
    	}
    	else {
    		return "Failed to save image";
    	}
    }
    
    public static String downloadImage(String url, String filename, String dlpath, boolean show_progress, Context context) throws IOException {
    	if (filename.equals("")) {
    		filename = url.substring(url.lastIndexOf("/") + 1);
    	}
		URL u = new URL(url);
		URLConnection uc = u.openConnection();
		
		if (uc == null) {
			Log.e("flickrfree", "Failed to open connection while trying to download \"" + url + "\".");
			return "fail: Failed to open connection";
		}
		
		double contentLength = (double)uc.getContentLength();
		double contentReceived = 0;

		if (uc.getContentType() == null || !uc.getContentType().contains("image")) {
			Log.e("flickrfree", "File at URL \"" + url + "\" is not an image.");
			return "fail: File is not an image";
		}

		// Check to see if download directory exists. If not, create it.
		File download = new File(dlpath);
		if (!download.exists()) {
			download.mkdir();
		}

		InputStream in = uc.getInputStream();
		if (in == null) {
			return "fail: Failed to get input stream";
		}
		else {
			File f = new File(dlpath,filename);
			FileOutputStream imgfile = new FileOutputStream(f);
			byte[] buffer = new byte[1024];
			int len1 = 0;
			double progress = 0, old_progress = 0;
			double broadcast_trigger = 1.0;
			Intent broadcast_intent = new Intent();
			broadcast_intent.setAction(GlobalResources.INTENT_DOWNLOAD_PROGRESS_UPDATE);
			while ((len1 = in.read(buffer)) != -1) {
				imgfile.write(buffer,0, len1);
				contentReceived += (double)1024;
				
				if (show_progress && context != null) {
					progress = 100.0 * contentReceived / contentLength;
					// We don't want to send a broadcast every time data is written,
					// so only do it when the amount written since the last broadcast
					// is at least 1% of the total size.
					if ((progress - old_progress) >= broadcast_trigger) {
						broadcast_intent.putExtra("percent", (int)Math.round(progress));
						broadcast_intent.putExtra("filename", filename);
						context.sendBroadcast(broadcast_intent);
						old_progress = progress;
					}
				}
			}
	
			in.close();
			imgfile.close();
		}
		
		return "success: Image path = " +  dlpath + "/" + filename;
    }

    public static boolean CheckDir(String dir_name) {
    	boolean result = false;
    	
        if (dir_name != null && !dir_name.equals("")) {
    		File Dir = new File(dir_name);
    		if (Dir.exists() || Dir.mkdir()) {
    			result = true;
    		}
        }
        
        return result;
    }
    
    public static String GetAppDir() {
        // Check for the app directory. If it doesn't exist, create it.
        String app_dir = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
        ? Environment.getExternalStorageDirectory().toString() + "/FlickrFree/"
        : "";
        
        return CheckDir(app_dir) ? app_dir : "";
    }
    
    public static String GetCacheDir(Activity callingActivity) {
    	if (callingActivity != null && callingActivity.getCacheDir() != null) {
    		return callingActivity.getCacheDir().getAbsolutePath();
    	}
    	else {
    		return "";
    	}
    }
    
    public static String GetDownloadDir() {
    	String app_dir = GetAppDir();
    	
    	// If the app directory can be found, set the cache directory to be the subdirectory
    	// "download" in the app directory. If not, there is no download path -- files cannot
    	// be downloaded.
        String dl_dir = (app_dir == null) || app_dir.equals("")
				        ? ""
				        : app_dir + "download";

        return CheckDir(dl_dir) ? dl_dir : "";
    }
    
    public static String CachedImageFilename(String url) {
		return (url.replaceAll(":", "").replace("/", ""));
    }
    
    public static boolean CacheImage(String url, Activity callingActivity, boolean show_progress) throws MalformedURLException, IOException, InterruptedException {
		String filename = CachedImageFilename(url);
		String cachedir = GetCacheDir(callingActivity);
		File img_cache = new File(cachedir + "/" + filename);
		
		// Check to see if a cached image with this name already exists. If not, then
		// download the image.
		for (int i = 0; i < NRETRIES && !(img_cache.exists()); i++) {
			if (i > 0) {
				sleep(ERROR_DELAY_MS);
				Log.e("flickrfree", "Error retrieving image from URL \"" + url + "\". Retrying.");
			}
			downloadImage(url, filename, cachedir, show_progress, callingActivity.getApplicationContext());
		}

    	return img_cache.exists();
    }
    
    public static Bitmap GetCachedImage(String url, Activity callingActivity) throws MalformedURLException, IOException, InterruptedException {
    	Bitmap b = null;
		File img_cache = new File(GetCacheDir(callingActivity)
									+ "/" + CachedImageFilename(url));
		
		if (img_cache.exists()) {
			b = BitmapFactory.decodeFile(img_cache.getAbsolutePath());
		}

    	return b;
    }
    
    public static String GetBuddyIcon(String nsid) {
    	return GetBuddyIcon(APICalls.peopleGetInfo(nsid));
    }
    
    public static String GetBuddyIcon(JSONObject userinfo) {
		int iconserver = JSONParser.getInt(userinfo, "person/iconserver");
		int iconfarm = JSONParser.getInt(userinfo, "person/iconfarm");
		String nsid = JSONParser.getString(userinfo, "person/nsid");
		String icon_url = "";
		
		if (iconserver > 0 && iconfarm > 0) {
			icon_url = "http://farm"
						+ iconfarm
						+ ".static.flickr.com/"
						+ iconserver + "/buddyicons/"
						+ nsid + ".jpg";
		}
		else {
			icon_url = "http://www.flickr.com/images/buddyicon.jpg";
		}
		
		return icon_url;
    }    

    public static double LatLongToDecimal(String val) {
    	double deg, min, sec;
    	String[] string_arr;
    	
    	string_arr = val.split(" deg ");
    	deg = Double.valueOf(string_arr[0]);
    	string_arr = string_arr[1].split("' ");
    	min = Double.valueOf(string_arr[0]);
    	String s = string_arr[1].substring(0, string_arr[1].length() - 1); 
    	sec = Double.valueOf(s);
    	
    	return LatLongToDecimal(deg, min, sec);
    }
    
    public static double LatLongToDecimal(double deg, double min, double sec) {
    	double val = (deg + (min / 60.0) + (sec / 3600.0));
    	
    	try {
    		return Double.valueOf((new DecimalFormat("#.#######")).format(val)).doubleValue();
    	} catch (NumberFormatException e) {
    		return 0.0;
    	}
    }
    
    public static void LogSharedPrefs(SharedPreferences pref) {
    	Map<String, ?> m = pref.getAll();
    	for (String key : m.keySet()) {
        	Log.i("flickrfree", "Prefs Entry: (" + key + ", " + m.get(key).toString() + ")");
    	}
    	
    }
    
    public static void sleep(long ms) {
		Thread.currentThread();
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
