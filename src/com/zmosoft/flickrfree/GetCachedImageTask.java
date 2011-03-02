package com.zmosoft.flickrfree;

import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class GetCachedImageTask extends AsyncTask<Object, Integer, Bitmap> {

	@Override
	protected Bitmap doInBackground(Object... params) {
		if (params.length < 3 || !(params[0] instanceof Activity) || !(params[1] instanceof ImageView)
				|| !(params[2] instanceof String)) {
				Log.e("flickrfree", "Invalid data passed to GetCachedImageTask instance");
				return null;
		}
		
		m_activity = (Activity)params[0];
		m_imageview = (ImageView)params[1];
		
		String img_url = (String)params[2];
		boolean show_progress = (params.length > 3 && params[3] instanceof Boolean)
								? (Boolean)params[3]
								: true;
		
    	Bitmap b = null;
    	try {
	    	if (GlobalResources.CacheImage(img_url, m_activity, show_progress)) {
	    		b = GlobalResources.GetCachedImage(img_url, m_activity);
	    	}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return b;
	}

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPreExecute() {
    }
    
    protected void onPostExecute(Bitmap image) {
		if (image == null) {
			Log.e("flickrfree", "Couldn't Download Picture.");
		}
		else {
			m_imageview.setImageBitmap(image);
		}
    	m_activity.setProgressBarIndeterminateVisibility(false);
    }
    
    ImageView m_imageview;
    Activity m_activity;
}
