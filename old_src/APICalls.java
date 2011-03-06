package com.zmosoft.flickrfree;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class APICalls {
	
	public static JSONObject peopleGetInfo(String userid) {
		return RestClient.CallFunction("flickr.people.getInfo", new String[]{"user_id"}, new String[]{userid});
	}
	
	public static JSONObject peopleFindByUsername(String username) {
		return RestClient.CallFunction("flickr.people.findByUsername", new String[]{"username"}, new String[]{username});
	}

	public static JSONObject peopleGetPhotos(String userid) {
		return APICalls.peopleGetPhotos(userid, 0, 0);
	}
	
	public static JSONObject peopleGetPhotos(String userid, int page) {
		return APICalls.peopleGetPhotos(userid, page, 0);
	}
	
	public static JSONObject peopleGetPhotos(String userid, int page, int per_page) {
    	ArrayList<String> paramNames = new ArrayList<String>();
    	ArrayList<String> paramVals = new ArrayList<String>();
    	String[] paramNames_arr = new String[]{};
    	String[] paramVals_arr = new String[]{};
    	
    	paramNames.add("user_id");
    	paramVals.add(userid);
    	if (per_page > 0) {
    		paramNames.add("per_page");
    		paramVals.add(String.valueOf(per_page));
    	}
    	if (page > 0) {
    		paramNames.add("page");
    		paramVals.add(String.valueOf(page));
    	}
		return RestClient.CallFunction("flickr.people.getPhotos",
									   paramNames.toArray(paramNames_arr),
									   paramVals.toArray(paramVals_arr));
	}

	public static JSONObject peopleGetPublicGroups(String userid) {
		return RestClient.CallFunction("flickr.people.getPublicGroups", new String[]{"user_id"}, new String[]{userid});
	}
	
    public static String getNameFromNSID(String nsid) throws JSONException {
		JSONObject result = peopleGetInfo(nsid);
		return (result.has("person") && result.getJSONObject("person").has("username")
				&& result.getJSONObject("person").getJSONObject("username").has("_content")
				? result.getJSONObject("person").getJSONObject("username").getString("_content")
				: "");
    }
    
    public static String getNSIDFromName(String username) throws JSONException {
		JSONObject result = peopleFindByUsername(username);
		return (result.has("user") && result.getJSONObject("user").has("nsid")
				? result.getJSONObject("user").getString("nsid")
				: "");
    }
    
    public static JSONObject photosCommentsGetList(String photo_id) {
        return RestClient.CallFunction("flickr.photos.comments.getList",new String[]{"photo_id"},new String[]{photo_id});
    }
    
    public static JSONObject photosCommentsAddComment(String photo_id, String comment) {
        return RestClient.CallFunction("flickr.photos.comments.addComment",new String[]{"photo_id", "comment_text"},new String[]{photo_id, comment});
    }
    
    public static JSONObject photosetsGetList(String userid) {
    	return RestClient.CallFunction("flickr.photosets.getList",new String[]{"user_id"},new String[]{userid});
    }
    
    public static JSONObject photosetsGetInfo(String setid) {
    	return RestClient.CallFunction("flickr.photosets.getInfo",new String[]{"photoset_id"},new String[]{setid});
    }

    public static JSONObject collectionsGetTree(String userid) {
		return RestClient.CallFunction("flickr.collections.getTree",new String[]{"user_id"},new String[]{userid});
    }

    public static JSONObject photosSearch(String userid, String text, String tags) {
    	return APICalls.photosSearch(userid, text, tags, 0, 0);
    }
    
    public static JSONObject photosSearch(String userid, String text, String tags, int page) {
    	return APICalls.photosSearch(userid, text, tags, page, 0);
    }
    
    public static JSONObject photosSearch(String userid, String text, String tags, int page, int per_page) {
    	ArrayList<String> paramNames = new ArrayList<String>();
    	ArrayList<String> paramVals = new ArrayList<String>();
    	String[] paramNames_arr = new String[]{};
    	String[] paramVals_arr = new String[]{};

    	paramNames.add("user_id");
    	paramVals.add(userid);
    	if (text != null && !text.equals("")) {
    		paramNames.add("text");
    		paramVals.add(text);
    	}
    	if (tags != null && !tags.equals("")) {
    		paramNames.add("tags");
    		paramVals.add(tags);
    	}
    	if (userid != null && !userid.equals("")) {
    		paramNames.add("userid");
    		paramVals.add(userid);
    	}
    	if (per_page > 0) {
    		paramNames.add("per_page");
    		paramVals.add(String.valueOf(per_page));
    	}
    	if (page > 0) {
    		paramNames.add("page");
    		paramVals.add(String.valueOf(page));
    	}

    	return RestClient.CallFunction("flickr.photos.search",
				   paramNames.toArray(paramNames_arr),
				   paramVals.toArray(paramVals_arr));
    }

    public static JSONObject photosGetInfo(String photoid) {
        return RestClient.CallFunction("flickr.photos.getInfo",new String[]{"photo_id"},new String[]{photoid});
    }
    
    public static JSONObject photosGetExif(String photoid) {
        return RestClient.CallFunction("flickr.photos.getExif",new String[]{"photo_id"},new String[]{photoid});
    }
    
    public static JSONObject photosGetSizes(String photoid) {
        return RestClient.CallFunction("flickr.photos.getSizes",new String[]{"photo_id"},new String[]{photoid});
    }
    
    public static JSONObject photosGetAllContexts(String photoid) {
        return RestClient.CallFunction("flickr.photos.getAllContexts",new String[]{"photo_id"},new String[]{photoid});
    }
    
    public static JSONObject ping() {
    	return RestClient.CallFunction("flickr.test.null");
    }
    
    public static JSONObject tagsGetListUser(String userid) {
    	return RestClient.CallFunction("flickr.tags.getListUser", new String[]{"user_id"}, new String[]{userid});
    }

    public static JSONObject groupsGetInfo(String groupid) {
        return RestClient.CallFunction("flickr.groups.getInfo",new String[]{"group_id"},new String[]{groupid});
    }
    
    public static JSONObject groupsPoolsGetGroups() {
        return RestClient.CallFunction("flickr.groups.pools.getGroups",null,null);
    }
    
    public static JSONObject groupsPoolsGetPhotos(String groupid) {
    	return APICalls.groupsPoolsGetPhotos(groupid, 0, 0);
    }
    
    public static JSONObject groupsPoolsGetPhotos(String groupid, int page) {
    	return APICalls.groupsPoolsGetPhotos(groupid, page, 0);
    }
    
    public static JSONObject groupsPoolsGetPhotos(String groupid, int page, int per_page) {
    	ArrayList<String> paramNames = new ArrayList<String>();
    	ArrayList<String> paramVals = new ArrayList<String>();
    	String[] paramNames_arr = new String[]{};
    	String[] paramVals_arr = new String[]{};
    	
    	paramNames.add("group_id");
    	paramVals.add(groupid);
    	if (per_page > 0) {
    		paramNames.add("per_page");
    		paramVals.add(String.valueOf(per_page));
    	}
    	if (page > 0) {
    		paramNames.add("page");
    		paramVals.add(String.valueOf(page));
    	}
    	return RestClient.CallFunction("flickr.groups.pools.getPhotos",
    								   paramNames.toArray(paramNames_arr),
    								   paramVals.toArray(paramVals_arr));
    }
    
    public static JSONObject groupsSearch(String text, String perpage) {
        return RestClient.CallFunction("flickr.groups.search",new String[]{"text", "per_page"},new String[]{text, perpage});
    }
    
    public static String getGroupNameFromID(String groupid) {
    	String name = "";
    	
    	JSONObject group_info = groupsGetInfo(groupid);
    	try {
			if (group_info.has("group") && group_info.getJSONObject("group").has("name")
				&& group_info.getJSONObject("group").getJSONObject("name").has("_content")) {
				name = group_info.getJSONObject("group").getJSONObject("name").getString("_content");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return name;
    }
    
    public static String[] getGroupInfoFromURL(String url) {
    	String[] info = null;
    	
    	JSONObject group_info = RestClient.CallFunction("flickr.urls.lookupGroup", new String[]{"url"}, new String[]{url});
    	try {
			if (group_info.has("group") && group_info.getJSONObject("group").has("groupname")
				&& group_info.getJSONObject("group").getJSONObject("groupname").has("_content")
				&& group_info.getJSONObject("group").has("id")) {
				String name = group_info.getJSONObject("group").getJSONObject("groupname").getString("_content");
				String id = group_info.getJSONObject("group").getString("id");
				info = new String[] {name, id};
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return info;
    }
    
    public static String getPhotoNameFromID(String photoid) {
    	String name = "";
    	
    	JSONObject photo_info = photosGetInfo(photoid);
    	try {
			if (photo_info.has("photo") && photo_info.getJSONObject("photo").has("title")
				&& photo_info.getJSONObject("photo").getJSONObject("title").has("_content")) {
				name = photo_info.getJSONObject("photo").getJSONObject("title").getString("_content");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return name;
    }
    
    public static boolean authCheckToken() {
		JSONObject json_obj = RestClient.CallFunction("flickr.auth.checkToken",null,null);
		boolean rval = false;
		try {
			rval = json_obj.has("stat") && json_obj.getString("stat").equals("ok");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return rval;
    }

    public static JSONObject getFullToken(String minitoken) {
		return RestClient.CallFunction("flickr.auth.getFullToken",new String[]{"mini_token"},new String[]{minitoken},false);
    }
    
    public static void favoritesAdd(String photoid) {
    	RestClient.CallFunction("flickr.favorites.add", new String[]{"photo_id"}, new String[]{photoid});
    }

    public static void favoritesRemove(String photoid) {
    	RestClient.CallFunction("flickr.favorites.remove", new String[]{"photo_id"}, new String[]{photoid});
    }

    public static JSONObject favoritesGetList(int page, int per_page) {
    	return RestClient.CallFunction("flickr.favorites.getList",
    									new String[]{"page", "per_page"},
    									new String[]{String.valueOf(page), String.valueOf(per_page)});
    }

}
