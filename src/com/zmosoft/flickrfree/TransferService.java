package com.zmosoft.flickrfree;

import java.io.IOException;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

public class TransferService extends Service {

	// This is the receiver that we use to update the percentage progress display
    // for the current download.
	public class NotificationProgressUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Notification notification = null;
			int id = -1;
	        if (intent.getAction().equals(GlobalResources.INTENT_DOWNLOAD_PROGRESS_UPDATE)) {
	        	notification = m_download_notification;
	        	id = GlobalResources.DOWNLOADER_ID;
	        }
	        else if (intent.getAction().equals(GlobalResources.INTENT_UPLOAD_PROGRESS_UPDATE)) {
	        	notification = m_upload_notification;
	        	id = GlobalResources.UPLOADER_ID;
	        }

	        if (notification != null && id > 0) {
	        	Bundle extras = intent.getExtras();
	        	if (extras != null && extras.containsKey("percent")) {
					RemoteViews nView = notification.contentView;
					nView.setProgressBar(R.id.prgNotification, 100, extras.getInt("percent"), false);
					notification.contentView = nView;
					((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(id, notification);
	        	}
	        }
		}
	}

	// AsyncTask to download a picture in the background.
	private class DownloadPictureTask extends AsyncTask<Void, String, Object> {
		
		@Override
		protected Object doInBackground(Void... params) {
			Bundle download_info = null;
			while (!m_downloads.isEmpty()) {
				download_info = m_downloads.get(0);
				if (download_info != null) {
			        String result;
					try {
						publishProgress(new String[]{"start", download_info.getString("title")});
						result = GlobalResources.downloadImage(download_info.getString("url"), 
																	  "",
																	  true,
																	  getApplicationContext());
						m_downloads.remove();
						publishProgress(new String[]{"finish", download_info.getString("title"), result});
					} catch (IOException e1) {
						e1.printStackTrace();
						result = "fail: I/O Error";
					}
			        if (result == null || result.contains("fail")) {
			        	String err_str = null;
						err_str = (result == null) ? "Unknown Failure" : result.substring(result.indexOf("fail: ") + 6);
						publishProgress("fail", err_str);
			        	m_downloads.clear();
			        }
				}
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... progress) {
			// onProgressUpdate is called each time a new download starts. This allows
			// us to update the notification text to let the user know which picture
			// is being downloaded.
			if (progress.length > 1) {
				Intent broadcast_intent = new Intent();
				String status = progress[0];
				if (status.equals("start")) {
					String title = progress[1];
					// Start the download status-bar notification.
					if (m_download_notification != null) {
						RemoteViews nView = m_download_notification.contentView;
						nView.setTextViewText(R.id.txtNotificationTitle, getResources().getString(R.string.downloading) + " \"" + title + "\"");
						nView.setProgressBar(R.id.prgNotification, 100, 0, false);
						m_download_notification.contentView = nView;
		
						((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(GlobalResources.DOWNLOADER_ID, m_download_notification);
					}

					// Send out a broadcast to let us know that a download is starting.
					broadcast_intent.setAction(GlobalResources.INTENT_DOWNLOAD_STARTED);
					getApplicationContext().sendBroadcast(broadcast_intent);
				}
				else if (status.equals("finish")) {
					// Send out a broadcast to let us know that an download has finished.
			        broadcast_intent.setAction(GlobalResources.INTENT_DOWNLOAD_FINISHED);
	        		getApplicationContext().sendBroadcast(broadcast_intent);
				}
				else if (status.equals("fail")) {
					broadcast_intent.setAction(GlobalResources.INTENT_DOWNLOAD_FAILED);
					broadcast_intent.putExtra("error", progress[1]);
					getApplicationContext().sendBroadcast(broadcast_intent);
					((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(GlobalResources.DOWNLOADER_ID);
					Toast.makeText(getApplicationContext(), "Flickr Download Failed:\n" + progress[1], Toast.LENGTH_SHORT).show();
					stopSelf();
				}
			}
		}
		
		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected void onPostExecute(Object result) {
			// When all downloads are finished, kill the status bar download notification and stop the
			// Downloader service.
			Toast.makeText(getApplicationContext(), "Download(s) Completed", Toast.LENGTH_SHORT).show();
			((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(GlobalResources.DOWNLOADER_ID);
			stopSelf();
		}
		
		public void addDownload(Bundle download_info) {
			if (m_downloads == null) {
				m_downloads = new LinkedList<Bundle>();
			}
			m_downloads.add(download_info);
		}
		
		public LinkedList<Bundle> getDownloads() {
			return new LinkedList<Bundle>(m_downloads);
		}
		
		private LinkedList<Bundle> m_downloads = null;
	}
	
	// AsyncTask to upload a picture in the background.
	private class UploadPictureTask extends AsyncTask<Void, String, Object> {
		
		@Override
		protected Object doInBackground(Void... params) {
			Bundle upload_info = null;
			while (m_uploads.size() > 0) {
				upload_info = m_uploads.get(0);
				if (upload_info != null) {
					publishProgress(new String[]{"start", upload_info.getString("title")});
			        JSONObject result = RestClient.UploadPicture(upload_info.getString("filename"),
							    			 upload_info.getString("title"),
								    		 upload_info.getString("comment"),
								    		 upload_info.getString("tags"),
								    		 upload_info.getBoolean("is_public"),
								    		 upload_info.getBoolean("is_friend"),
								    		 upload_info.getBoolean("is_family"),
								    		 upload_info.getInt("safety_level"),
								    		 getApplicationContext());
					publishProgress(new String[]{"start", upload_info.getString("title")});
					m_uploads.remove();
			        if (result == null || result.has("fail")) {
			        	String err_str = null;
						try {
							err_str = (result == null) ? "Unknown Failure" : result.getString("fail");
						} catch (JSONException e) {
							err_str = "Unknown Failure";
						}
						publishProgress("fail", err_str);
			        	m_uploads.clear();
			        }
				}
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... progress) {
			// onProgressUpdate is called each time a new upload starts. This allows
			// us to update the notification text to let the user know which picture
			// is being uploaded.
			if (progress.length > 1) {
				String status = progress[0];
				Intent broadcast_intent = new Intent();
				if (status.equals("start")) {
					// Start the progress status-bar notification
					if (m_upload_notification != null) {
						RemoteViews nView = m_upload_notification.contentView;
						nView.setTextViewText(R.id.txtNotificationTitle, getResources().getString(R.string.uploading) + " \"" + progress[1] + "\"");
						nView.setProgressBar(R.id.prgNotification, 100, 0, false);
						m_upload_notification.contentView = nView;
		
						((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(GlobalResources.UPLOADER_ID, m_upload_notification);
					}

					// Send out a broadcast to let us know that an upload is starting.
					broadcast_intent.setAction(GlobalResources.INTENT_UPLOAD_STARTED);
					getApplicationContext().sendBroadcast(broadcast_intent);
				}
				if (status.equals("finish")) {
					// Send out a broadcast to let us know that an upload is starting.
					broadcast_intent.setAction(GlobalResources.INTENT_UPLOAD_FINISHED);
					getApplicationContext().sendBroadcast(broadcast_intent);
				}
				else if (status == "fail") {
					broadcast_intent.setAction(GlobalResources.INTENT_UPLOAD_FAILED);
					broadcast_intent.putExtra("error", progress[1]);
					getApplicationContext().sendBroadcast(broadcast_intent);

					((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(GlobalResources.UPLOADER_ID);
					Toast.makeText(getApplicationContext(), "Flickr Upload Failed:\n" + progress[1], Toast.LENGTH_SHORT).show();
					stopSelf();
				}
			}
		}
		
		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected void onPostExecute(Object result) {
			// When all uploads are finished, kill the status bar upload notification and stop the
			// Uploader service.
			Toast.makeText(getApplicationContext(), "Upload(s) Completed", Toast.LENGTH_SHORT).show();
			((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(GlobalResources.UPLOADER_ID);
			stopSelf();
		}
		
		public void addUpload(Bundle upload_info) {
			if (m_uploads == null) {
				m_uploads = new LinkedList<Bundle>();
			}
			m_uploads.add(upload_info);
		}
		
		public LinkedList<Bundle> getUploads() {
			return new LinkedList<Bundle>(m_uploads);
		}
		
		private LinkedList<Bundle> m_uploads = null;
	}
	
	public class TransferServiceBinder extends Binder {
        TransferService getService() {
            return TransferService.this;
        }
    }

	@Override
	public void onDestroy () {
		super.onDestroy();
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(GlobalResources.DOWNLOADER_ID);
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(GlobalResources.UPLOADER_ID);
		if (m_update_receiver != null) {
			this.unregisterReceiver(m_update_receiver);
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			if (extras.getString("type").equals("download")) {
				addDownload(intent.getExtras());
			}
			else if (extras.getString("type").equals("upload")) {
				addUpload(intent.getExtras());
			}
		}
		m_update_receiver = new NotificationProgressUpdateReceiver();
		if (m_update_receiver != null) {
			IntentFilter filter = new IntentFilter(GlobalResources.INTENT_DOWNLOAD_PROGRESS_UPDATE);
			filter.addAction(GlobalResources.INTENT_UPLOAD_PROGRESS_UPDATE);
			this.registerReceiver(m_update_receiver, filter);
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return m_binder;
	}

	public void addDownload(Bundle download_info) {
		if (m_download_task == null || (m_download_task.getStatus() == AsyncTask.Status.FINISHED)) {
			// If the download task has not yet been created or if it is finished, then create
			// a new download task, add the download to it, and execute.
			m_download_task = (DownloadPictureTask)new DownloadPictureTask();
			m_download_task.addDownload(download_info);
			m_download_task.execute();
		}
		else {
			// Otherwise, the download task is currently running, so add the new download to the
			// list.
			m_download_task.addDownload(download_info);
		}

		if (m_download_notification == null) {
			// Create the status bar notification that will be displayed.
			CharSequence tickerText = this.getString(R.string.downloadingpicture);
			m_download_notification = new Notification(android.R.drawable.stat_sys_download, tickerText, System.currentTimeMillis());
			m_notify_activity = PendingIntent.getActivity(this, 0, new Intent(this, TransferProgress.class), 0);
			m_download_notification.contentIntent = m_notify_activity;

			RemoteViews nView = new RemoteViews(getPackageName(), R.layout.progress_notification_layout);
			//nView.setImageViewResource(R.id.imgIcon, R.drawable.icon);
			nView.setTextViewText(R.id.txtNotificationTitle, getResources().getString(R.string.downloading) + " \"" + download_info.getString("title") + "\"");
			m_download_notification.contentView = nView;
			m_download_notification.flags = Notification.FLAG_ONGOING_EVENT;
		}
	}
	
	public void addUpload(Bundle upload_info) {
		if (m_upload_task == null || (m_upload_task.getStatus() == AsyncTask.Status.FINISHED)) {
			// If the upload task has not yet been created or if it is finished, then create
			// a new upload task, add the upload to it, and execute.
			m_upload_task = (UploadPictureTask)new UploadPictureTask();
			m_upload_task.addUpload(upload_info);
			m_upload_task.execute();
		}
		else {
			// Otherwise, the upload task is currently running, so add the new upload to the
			// list.
			m_upload_task.addUpload(upload_info);
		}

		if (m_upload_notification == null) {
			// Create the status bar notification that will be displayed.
			CharSequence tickerText = this.getString(R.string.uploadingpicture);
			m_upload_notification = new Notification(android.R.drawable.stat_sys_upload, tickerText, System.currentTimeMillis());
			m_notify_activity = PendingIntent.getActivity(this, 0, new Intent(this, TransferProgress.class), 0);
			m_upload_notification.contentIntent = m_notify_activity;

			RemoteViews nView = new RemoteViews(getPackageName(), R.layout.progress_notification_layout);
			//nView.setImageViewResource(R.id.imgIcon, R.drawable.icon);
			nView.setTextViewText(R.id.txtNotificationTitle, getResources().getString(R.string.uploading) + " \"" + upload_info.getString("title") + "\"");
			m_upload_notification.contentView = nView;
			m_upload_notification.flags = Notification.FLAG_ONGOING_EVENT;
		}
	}
	
	public LinkedList<Bundle> getDownloads() {
		if (m_download_task == null) {
			return new LinkedList<Bundle>();
		}
		else {
			return m_download_task.getDownloads();
		}
	}
	
	public LinkedList<Bundle> getUploads() {
		if (m_upload_task == null) {
			return new LinkedList<Bundle>();
		}
		else {
			return m_upload_task.getUploads();
		}
	}

	private Notification m_upload_notification = null;
	private Notification m_download_notification = null;
	private NotificationProgressUpdateReceiver m_update_receiver = null;
	private PendingIntent m_notify_activity = null;
	private final IBinder m_binder = new TransferServiceBinder();
	private DownloadPictureTask m_download_task = null;
	private UploadPictureTask m_upload_task = null;
}
