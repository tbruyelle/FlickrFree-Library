package com.zmosoft.flickrfree;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class PictureSendReceiver extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RestClient.setAuth(this);
        Intent intent = getIntent();
		if (intent.getAction().equals(Intent.ACTION_SEND)) {
			Bundle extras = intent.getExtras();
			if (extras.containsKey("android.intent.extra.STREAM")) {
				Uri uri = (Uri)extras.get("android.intent.extra.STREAM");
				Intent i = new Intent(this, PictureSettings.class);
				i.putExtra("image_uri", uri.toString());
				i.putExtra("action", "upload");
				startActivity(i);
			}
		}
		finish();
	}
}
