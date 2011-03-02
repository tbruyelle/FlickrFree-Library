package com.zmosoft.flickrfree;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;

import android.webkit.WebView;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ImageComments extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagecomments);
        ((RelativeLayout)findViewById(R.id.ImgAddCommentLayout)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.BtnAddComment)).setVisibility(View.VISIBLE);

        ((Button)findViewById(R.id.BtnAddComment)).setOnClickListener(this);
        ((Button)findViewById(R.id.BtnAddCommentSend)).setOnClickListener(this);
        ((Button)findViewById(R.id.BtnAddCommentCancel)).setOnClickListener(this);

        RestClient.setAuth(this);
        
        m_extras = getIntent().getExtras();
        m_comment_list = APICalls.photosCommentsGetList(m_extras.getString("photo_id"));
        
        m_comments_per_page = 20;
        m_current_page = 0;
        
        FillCommentList();
    }

    private void ClearCommentList() {
    	LinearLayout comment_layout = (LinearLayout)findViewById(R.id.ImgCommentsLayout);
    	// Delete all of the children of the comment layout -- except the first. That
    	// one is not a comment; it is the layout for the "Add Comments" section.
    	while (comment_layout.getChildCount() > 1) {
    		comment_layout.removeViewAt(1);
    	}
    }
    
    private void FillCommentList() {
    	FillCommentList(0);
    }
    
    private void FillCommentList(int start) {
    	try {
			LinkedHashMap<String,String> comments = new LinkedHashMap<String,String>();
			JSONArray comment_arr = JSONParser.getArray(m_comment_list, "comments/comment");
			
			// If no comments exist, then we will get a null objecct for the comment array.
			// Replace this with an empty JSONArray to avoid null pointer errors.
			if (comment_arr == null) {
				comment_arr = new JSONArray();
			}

			// If the number of comments exceeds the maximum comments per page, then set
			// the "end" variable so we only interate through the comments that will be
			// displayed on this page.
			int end = comment_arr.length() > (start + m_comments_per_page)
					? start + m_comments_per_page
					: comment_arr.length();
			
			JSONObject comment_obj;
			for (int i = start; i < end; i++) {
				comment_obj = null;
				comment_obj = comment_arr.getJSONObject(i);
				String author_name = JSONParser.getString(comment_obj, "authorname");
				String comment = JSONParser.getString(comment_obj, "_content");
				if (author_name != null && comment != null) {
					comments.put(author_name, comment);
				}
			}

			if (comment_arr.length() > 0) {
				((TextView)findViewById(R.id.ImgCommentCount)).setText(getResources().getString(R.string.lblwordcomments)
						                                               + " "
						                                               + (start+1)
						                                               + " - "
						                                               + end
						                                               + " "
						                                               + getResources().getString(R.string.lblwordof)
						                                               + " "
						                                               + String.valueOf(comment_arr.length()));
			}
			else {
				((TextView)findViewById(R.id.ImgCommentCount)).setText(getResources().getString(R.string.lblnocomments));
			}

			CommentLayout entry;
			String comment = "";
			LinearLayout comment_layout = ((LinearLayout)findViewById(R.id.ImgCommentsLayout));
			for (String key : comments.keySet()) {
				// Add the title/value entry pair for the set of comments.
				entry = (CommentLayout)View.inflate(this, R.layout.image_comment_entry, null);
				comment = comments.get(key);

				// Make the comment view clickable if there are links to photos or groups
				// in the comment.
				boolean found_links = ReadLinksInComment(entry, comment);
				entry.findViewById(R.id.CommentLinkButton).setVisibility(found_links ? View.VISIBLE : View.GONE);
				entry.setClickable(found_links);
				if (entry.isClickable()) {
					entry.setOnClickListener(this);
				}

				comment = diableAllURLsInComment(comment);
				((TextView)entry.findViewById(R.id.Author)).setText(key);
				((WebView)entry.findViewById(R.id.Comment)).loadData(comment, "text/html", "utf-8");
				comment_layout.addView(entry);
			}
			
			// If the comment array exceeds the number of comments displayed on this page,
			// then show the "More Comments" button.
			if (comment_arr.length() > end) {
				View v = View.inflate(this, R.layout.entry_more_comments, null);
				v.findViewById(R.id.BtnMoreComments).setOnClickListener(this);
				
				comment_layout.addView(v);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    public boolean ReadLinksInComment(CommentLayout entry, String comment) {
    	boolean links_found = false;
    	
    	Integer[] url_pos = findURLPosInComment(comment);
    	
    	while (url_pos != null) {
			String url_str = comment.substring(url_pos[0], url_pos[1]);
			URL url;
    		try {
    			if (url_str.substring(0, 1).equals("/")) {
    				// If the URL starts with "/", then it is probably a relative URL.
    				// We need to prepend the rest of the Flickr URL onto it so it can
    				// be read correctly.
    				url_str = "http://www.flickr.com" + url_str;
    			}
    			else if (url_str.substring(0, 3).equals("www")) {
    				url_str = "http://" + url_str;
    			}
    			url = new URL(url_str);
			} catch (MalformedURLException e) {
		    	url_pos = findURLPosInComment(comment, url_pos[1] + 1);
				continue;
			}
			String path = url.getPath();
			if (url.getProtocol().equals("http")) {
				if (path.contains("groups")) {
					String[] groupinfo = APICalls.getGroupInfoFromURL(url.toString());
					if (groupinfo != null) {
			    		links_found = true;
						entry.m_group_links.put(groupinfo[0], groupinfo[1]);
					}
				}
			}
	    	url_pos = findURLPosInComment(comment, url_pos[1] + 1);
    	}
    	
    	return links_found;
    }
    
    private Integer[] findURLPosInComment(String comment) {
    	return findURLPosInComment(comment, 0);
    }
    
    private Integer[] findURLPosInComment(String comment, int start) {
    	String start_string = "<a href=\"";
    	int href_start = comment.indexOf(start_string, start);
    	int href_end = comment.indexOf("\"", href_start + start_string.length());
    	Integer[] range = null;
    	
    	if (href_start >= 0) {
    		href_start += start_string.length();
    		range = new Integer[]{href_start, href_end};
    	}
    	
    	return range;
    }
    
    private String diableAllURLsInComment(String comment) {
    	int pos = 0;
    	
    	while (pos >= 0) {
    		comment = disableURLInComment(comment, pos);
    		pos = comment.indexOf("<a href=");
    	}
    	return comment;
    }
    
    private String disableURLInComment(String comment, int start) {
    	String href_open_start = "<a";
    	String href_open_end = "\">";
    	String href_close = "</a>";
    	String part_a, part_b = null;
    	
    	int href_open_start_pos = comment.indexOf(href_open_start, start);
    	int href_open_end_pos = comment.indexOf(href_open_end, start);
    	int href_close_pos = comment.indexOf(href_close, start);
    	
    	if (href_open_start_pos >= 0 && href_open_end_pos >= 0 && href_close_pos >= 0) {
	    	part_a = comment.substring(0, href_open_start_pos + 1);
	    	part_b = comment.substring(href_open_end_pos + 1);
	    	comment = part_a + "b style=\"background: #0066FF; color: white;\"" + part_b;
	    	
	    	href_close_pos = comment.indexOf(href_close, start);
	    	part_a = comment.substring(0, href_close_pos);
	    	part_b = comment.substring(href_close_pos + 4);
	    	comment = part_a + "</b>" + part_b;
	    	
    	}
    	
    	return comment;
    }
    
	@Override
	public void onClick(View v) {
		if (v instanceof CommentLayout) {
			JSONObject group_links = new JSONObject(((CommentLayout)v).m_group_links);
			
			Intent i = new Intent(this, CommentLinkView.class);
			i.putExtra("groups", group_links.toString());
			startActivity(i);
		}
		else if (v.getId() == R.id.BtnAddComment) {
	        ((RelativeLayout)findViewById(R.id.ImgAddCommentLayout)).setVisibility(View.VISIBLE);
	        ((Button)findViewById(R.id.BtnAddComment)).setVisibility(View.GONE);
		}
		else if (v.getId() == R.id.BtnAddCommentCancel) {
	        ((RelativeLayout)findViewById(R.id.ImgAddCommentLayout)).setVisibility(View.GONE);
	        ((Button)findViewById(R.id.BtnAddComment)).setVisibility(View.VISIBLE);
		}
		else if (v.getId() == R.id.BtnAddCommentSend) {
			String comment = ((EditText)findViewById(R.id.CommentText)).getText().toString();
			APICalls.photosCommentsAddComment(m_extras.getString("photo_id"), comment);
			
	        ((RelativeLayout)findViewById(R.id.ImgAddCommentLayout)).setVisibility(View.GONE);
	        ((Button)findViewById(R.id.BtnAddComment)).setVisibility(View.VISIBLE);
		}
		else if (v.getId() == R.id.BtnMoreComments) {
			((ScrollView)findViewById(R.id.ImgCommentsScroll)).smoothScrollTo(0,0);
			ClearCommentList();
			++m_current_page;
			FillCommentList(m_comments_per_page * m_current_page);
		}
	}

	Bundle m_extras;
	JSONObject m_comment_list;
	
	int m_comments_per_page;
	int m_current_page;
}
