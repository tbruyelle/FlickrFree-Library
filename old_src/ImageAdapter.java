package com.zmosoft.flickrfree;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

	public ImageAdapter(Activity activity, int size) {
	    m_urlmap = new HashMap<Integer, String>();
        m_size = size <= GlobalResources.IMGS_PER_PAGE ? size : GlobalResources.IMGS_PER_PAGE;
        m_activity = activity;
    }

    public int getCount() {
		return m_size;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	View itemView = (convertView == null)
    				  ? View.inflate((Context)m_activity, R.layout.image_grid_item, null)
    				  : convertView;
        
		try {
			Bitmap b = null;
			if (m_urlmap.containsKey(position)) {
				b = GlobalResources.GetCachedImage(m_urlmap.get(position), m_activity);
			}
			
			if (b == null) {
				b = BitmapFactory.decodeResource(m_activity.getResources(), R.drawable.img_wait);
			}
	   		((ImageView)itemView.findViewById(R.id.ImageViewItem)).setImageBitmap(b);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
		return itemView;
    }

    public void setImgUrl(int position, String img_url) {
    	m_urlmap.put(position, img_url);
    }
    
    Activity m_activity;
    int m_size;
    HashMap<Integer, String> m_urlmap;
}
