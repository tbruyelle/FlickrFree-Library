package com.zmosoft.flickrfree;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageInfo extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageinfo);
        
        RestClient.setAuth(this);
        
        m_extras = getIntent().getExtras();
        try {
			m_imginfo = m_extras.containsKey("imginfo") ? new JSONObject(m_extras.getString("imginfo"))
						: new JSONObject("");
			m_exif = m_extras.containsKey("exif") ? new JSONObject(m_extras.getString("exif"))
						: new JSONObject("");
		} catch (JSONException e) {
			e.printStackTrace();
		}
        FillTable();
    }

    private void FillTable() {
    	try {
			LinkedHashMap<String,String> info = new LinkedHashMap<String,String>();
			HashMap<String,String> exif = new LinkedHashMap<String,String>();
			
			JSONArray exif_arr = JSONParser.getArray(m_exif, "photo/exif");
			JSONObject exif_entry;
			for (int i = 0; exif_arr != null && i < exif_arr.length(); i++) {
				exif_entry = null;
				exif_entry = exif_arr.getJSONObject(i);
				exif.put(JSONParser.getString(exif_entry, "label"), JSONParser.getString(exif_entry, "raw/_content"));
			}

			JSONObject owner = JSONParser.getObject(m_imginfo, "photo/owner");
			String realname = JSONParser.getString(owner, "realname");
			String username = JSONParser.getString(owner, "username");
			String owner_str = "";
			if (realname != null && username != null) {
				owner_str = username;
				if (!realname.equals("")) {
					owner_str = realname + " (" + username + ")";
				}
			}
			
			if (!owner_str.equals("")) {
				info.put(getResources().getString(R.string.imageinfo_owner),owner_str);
			}

			String location = JSONParser.getString(owner, "location");
			if (!location.equals("")) {
				info.put(getResources().getString(R.string.imageinfo_owner_location),location);
			}

			String date_taken = JSONParser.getString(m_imginfo, "photo/dates/taken");
			if (date_taken != null) {
				info.put(getResources().getString(R.string.imageinfo_datetaken),date_taken);
			}

			if (exif.containsKey("GPS Latitude") && exif.containsKey("GPS Longitude")) {
				String latitude = String.valueOf(GlobalResources.LatLongToDecimal(exif.get("GPS Latitude")));
				String longitude = String.valueOf(GlobalResources.LatLongToDecimal(exif.get("GPS Longitude")));
				if (exif.containsKey("GPSLatitudeRef") && exif.containsKey("GPSLongitudeRef")) {
					latitude += exif.get("GPSLatitudeRef").substring(0, 1); 
					longitude += exif.get("GPSLongitudeRef").substring(0, 1); 
				}
				info.put(getResources().getString(R.string.imageinfo_locationtaken),
						 latitude + ", " + longitude);
			}
			else {
				JSONObject latitude = JSONParser.getObject(m_imginfo, "photo/location/latitude");
				JSONObject longitude = JSONParser.getObject(m_imginfo, "photo/location/longitude");
				if (latitude != null && longitude != null) {
					info.put(getResources().getString(R.string.imageinfo_locationtaken),
							 latitude + ", " + longitude);
				}
			}
			
			String camera = (exif.containsKey("Make") ? exif.get("Make") : "")
						+	(exif.containsKey("Model") ? " " + exif.get("Model") : "");
			if (!camera.equals("")) {
				info.put(getResources().getString(R.string.imageinfo_camera), camera);
			}
			
			String description = JSONParser.getString(m_imginfo, "photo/description/_content");
			if (description != null && !description.equals("")) {
				info.put(getResources().getString(R.string.imageinfo_description), description);
			}
	
			View entry;
			ImageButton entry_button;
			for (String key : info.keySet()) {
				// Add the title/value entry pair for this set of information.
				entry = View.inflate(this, R.layout.image_info_entry, null);
				entry_button = ((ImageButton)entry.findViewById(R.id.btnImageInfo));

				// Set up the button (if there is one) for this entry.
				entry_button.setOnClickListener(this);
				entry_button.setVisibility(View.VISIBLE);
				if (key.equals(getResources().getString(R.string.imageinfo_owner))) {
					String nsid = JSONParser.getString(owner, "nsid");
					String icon_url = GlobalResources.GetBuddyIcon(nsid);
					try {
						if (icon_url != "" && GlobalResources.CacheImage(icon_url, this, false)) {
							Bitmap buddyicon = GlobalResources.GetCachedImage(icon_url, this);
							entry_button.setImageBitmap(buddyicon);
							entry_button.setTag("user_page");
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				else {
					entry_button.setVisibility(View.GONE);
				}
				
				// Set the content of the title and value for this entry.
				((TextView)entry.findViewById(R.id.InfoTitle)).setText(key);
				((TextView)entry.findViewById(R.id.InfoValue)).setText(Html.fromHtml(info.get(key)));
				
				((LinearLayout)findViewById(R.id.ImgInfoLayout)).addView(entry);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnImageInfo) {
			if (((ImageButton)v).getTag().equals("user_page")) {
				try {
					String username = JSONParser.getString(m_imginfo, "photo/owner/username");
					String nsid = APICalls.getNSIDFromName(username);
		
					Intent i = new Intent(this, UserView.class);
					i.putExtra("nsid", nsid);
					try {
						startActivity(i);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	Bundle m_extras;
    JSONObject m_imginfo;
    JSONObject m_exif;
}
