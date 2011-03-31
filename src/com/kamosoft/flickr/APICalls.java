package com.kamosoft.flickr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.kamosoft.flickr.model.FlickrApiResult;

public class APICalls
{
    private static final int CONNECTION_TIMEOUT = 15000000;

    private static String m_RESTURL = "http://api.flickr.com/services/rest/";

    public static String m_EDITPERMS_URL = "http://www.flickr.com/services/auth/list.gne";

    /**
     * @param methodName
     * @param paramNames
     * @param paramVals
     * @param flickrParameters
     * @param authenticated
     * @return
     * @throws IOException
     */
    public static FlickrApiResult perform( String methodName, String[] paramNames, String[] paramVals,
                                           FlickrParameters flickrParameters, boolean authenticated )
        throws IOException
    {
        Log.d( "Start call " + methodName );
        HttpClient httpclient = new DefaultHttpClient();
        HttpParams http_params = httpclient.getParams();
        http_params.setParameter( CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1 );
        HttpConnectionParams.setConnectionTimeout( http_params, CONNECTION_TIMEOUT );
        HttpConnectionParams.setSoTimeout( http_params, CONNECTION_TIMEOUT );

        if ( paramNames == null )
        {
            paramNames = new String[0];
        }
        if ( paramVals == null )
        {
            paramVals = new String[0];
        }

        if ( paramNames.length != paramVals.length )
        {
            return null;
        }

        String url = m_RESTURL + "?method=" + methodName + "&api_key=" + flickrParameters.getApiKey();
        for ( int i = 0; i < paramNames.length; i++ )
        {
            try
            {
                String paramVal = URLEncoder.encode( paramVals[i], "UTF-8" );
                url += "&" + paramNames[i] + "=" + paramVal;
            }
            catch ( UnsupportedEncodingException e )
            {
                Log.e( "RestClient UnsupportedEncodingException while buliding REST URL" );
                Log.e( "\t" + e.getLocalizedMessage() );
            }
        }
        if ( authenticated )
        {
            url += "&auth_token=" + flickrParameters.getFullToken();
        }
        // Generate the signature
        String signature = "";
        SortedMap<String, String> sig_params = new TreeMap<String, String>();
        sig_params.put( "api_key", flickrParameters.getApiKey() );
        sig_params.put( "method", methodName );
        sig_params.put( "format", "json" );
        for ( int i = 0; i < paramNames.length; i++ )
        {
            sig_params.put( paramNames[i], paramVals[i] );
        }
        if ( authenticated )
        {
            sig_params.put( "auth_token", flickrParameters.getFullToken() );
        }
        signature = flickrParameters.getApiSecret();
        for ( Map.Entry<String, String> entry : sig_params.entrySet() )
        {
            signature += entry.getKey() + entry.getValue();
        }
        try
        {
            signature = JavaMD5Sum.computeSum( signature ).toLowerCase();
        }
        catch ( NoSuchAlgorithmException e1 )
        {
            e1.printStackTrace();
        }

        url += "&api_sig=" + signature + "&format=json";

        // Replace any spaces in the URL with "+".
        url = url.replace( " ", "+" );
        Log.d( "Prepare call url " + url );
        HttpResponse response = null;

        try
        {
            HttpGet httpget = new HttpGet( url );
            response = httpclient.execute( httpget );
        }
        catch ( ClientProtocolException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            throw e;
        }

        // Get hold of the response entity
        HttpEntity entity = null;
        if ( response != null )
        {
            entity = response.getEntity();
        }

        // If the response does not enclose an entity, there is no need
        // to worry about connection release
        if ( entity != null )
        {
            // A Simple JSON Response Read
            InputStream instream = entity.getContent();
            FlickrApiResult flickrApiResult = transform( convertStreamToString( instream ) );
            if ( !flickrApiResult.isStatusOk() )
            {
                Log.w( "Method returns fail status, code=\"" + flickrApiResult.getCode() + "\" message=\""
                    + flickrApiResult.getMessage() + "\"" );
            }
            return flickrApiResult;
        }
        Log.e( "Unable to retrieve result" );
        return null;
    }

    private static FlickrApiResult transform( String result )
    {
        if ( result == null )
        {
            Log.e( "transform with null parameter" );
            return null;
        }
        Log.d( "json=" + result );
        try
        {
            /* remove the non-json string jsonFlickrApi( "*" ) */
            String json = result.substring( 14, result.length() - 2 );
            FlickrApiResult flickrApiResult = new Gson().fromJson( json, FlickrApiResult.class );

            return flickrApiResult;
        }
        catch ( JsonParseException e )
        {
            Log.e( "JsonParseException during parsing of \n" + result );
            Log.e( e.getMessage(), e );
        }
        return null;
    }

    private static String convertStreamToString( InputStream is )
    {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
        StringBuilder sb = new StringBuilder();

        String line = null;
        try
        {
            while ( ( line = reader.readLine() ) != null )
            {
                sb.append( line + "\n" );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                is.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public static FlickrApiResult getFullToken( String minitoken, FlickrParameters flickrParameters )
        throws IOException
    {
        return perform( "flickr.auth.getFullToken", new String[] { "mini_token" }, new String[] { minitoken },
                        flickrParameters, false );
    }

    public static boolean checkNetwork( Context context, FlickrParameters flickrParameters )
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );

        NetworkInfo.State wifi_state = cm.getNetworkInfo( ConnectivityManager.TYPE_WIFI ).getState();
        NetworkInfo.State mobile_state = cm.getNetworkInfo( ConnectivityManager.TYPE_MOBILE ).getState();

        if ( ( wifi_state == NetworkInfo.State.CONNECTED ) || ( mobile_state == NetworkInfo.State.CONNECTED ) )
        {
            return ping( flickrParameters );
        }
        else
        {
            return false;
        }
    }

    public static boolean ping( FlickrParameters flickrParameters )
    {
        try
        {
            FlickrApiResult flickrApiResult = perform( "flickr.test.echo", null, null, flickrParameters, false );
            return ( flickrApiResult != null && flickrApiResult.isStatusOk() );
        }
        catch ( IOException e )
        {
            return false;
        }
    }

    /*
     * Methods from flirck-free not migred to return a FlicrApiResult 
     */

    //    public static JSONObject peopleGetInfo( String userid )
    //    {
    //        return RestClient.CallFunction( "flickr.people.getInfo", new String[] { "user_id" }, new String[] { userid } );
    //    }
    //
    //    public static JSONObject peopleFindByUsername( String username )
    //    {
    //        return RestClient.CallFunction( "flickr.people.findByUsername", new String[] { "username" },
    //                                        new String[] { username } );
    //    }
    //
    //    public static JSONObject peopleGetPhotos( String userid )
    //    {
    //        return APICalls.peopleGetPhotos( userid, 0, 0 );
    //    }
    //
    //    public static JSONObject peopleGetPhotos( String userid, int page )
    //    {
    //        return APICalls.peopleGetPhotos( userid, page, 0 );
    //    }
    //
    //    public static JSONObject peopleGetPhotos( String userid, int page, int per_page )
    //    {
    //        ArrayList<String> paramNames = new ArrayList<String>();
    //        ArrayList<String> paramVals = new ArrayList<String>();
    //        String[] paramNames_arr = new String[] {};
    //        String[] paramVals_arr = new String[] {};
    //
    //        paramNames.add( "user_id" );
    //        paramVals.add( userid );
    //        if ( per_page > 0 )
    //        {
    //            paramNames.add( "per_page" );
    //            paramVals.add( String.valueOf( per_page ) );
    //        }
    //        if ( page > 0 )
    //        {
    //            paramNames.add( "page" );
    //            paramVals.add( String.valueOf( page ) );
    //        }
    //        return RestClient.CallFunction( "flickr.people.getPhotos", paramNames.toArray( paramNames_arr ),
    //                                        paramVals.toArray( paramVals_arr ) );
    //    }
    //
    //    public static JSONObject peopleGetPublicGroups( String userid )
    //    {
    //        return RestClient.CallFunction( "flickr.people.getPublicGroups", new String[] { "user_id" },
    //                                        new String[] { userid } );
    //    }
    //
    //    public static String getNameFromNSID( String nsid )
    //        throws JSONException
    //    {
    //        JSONObject result = peopleGetInfo( nsid );
    //        return ( result.has( "person" ) && result.getJSONObject( "person" ).has( "username" )
    //            && result.getJSONObject( "person" ).getJSONObject( "username" ).has( "_content" ) ? result
    //            .getJSONObject( "person" ).getJSONObject( "username" ).getString( "_content" ) : "" );
    //    }
    //
    //    public static String getNSIDFromName( String username )
    //        throws JSONException
    //    {
    //        JSONObject result = peopleFindByUsername( username );
    //        return ( result.has( "user" ) && result.getJSONObject( "user" ).has( "nsid" ) ? result.getJSONObject( "user" )
    //            .getString( "nsid" ) : "" );
    //    }
    //
    //    public static JSONObject photosCommentsGetList( String photo_id )
    //    {
    //        return RestClient.CallFunction( "flickr.photos.comments.getList", new String[] { "photo_id" },
    //                                        new String[] { photo_id } );
    //    }
    //
    //    public static JSONObject photosCommentsAddComment( String photo_id, String comment )
    //    {
    //        return RestClient
    //            .CallFunction( "flickr.photos.comments.addComment", new String[] { "photo_id", "comment_text" },
    //                           new String[] { photo_id, comment } );
    //    }
    //
    //    public static JSONObject photosetsGetList( String userid )
    //    {
    //        return RestClient
    //            .CallFunction( "flickr.photosets.getList", new String[] { "user_id" }, new String[] { userid } );
    //    }
    //
    //    public static JSONObject collectionsGetTree( String userid )
    //    {
    //        return RestClient.CallFunction( "flickr.collections.getTree", new String[] { "user_id" },
    //                                        new String[] { userid } );
    //    }
    //
    //    public static JSONObject photosSearch( String userid, String text, String tags )
    //    {
    //        return APICalls.photosSearch( userid, text, tags, 0, 0 );
    //    }
    //
    //    public static JSONObject photosSearch( String userid, String text, String tags, int page )
    //    {
    //        return APICalls.photosSearch( userid, text, tags, page, 0 );
    //    }
    //
    //    public static JSONObject photosSearch( String userid, String text, String tags, int page, int per_page )
    //    {
    //        ArrayList<String> paramNames = new ArrayList<String>();
    //        ArrayList<String> paramVals = new ArrayList<String>();
    //        String[] paramNames_arr = new String[] {};
    //        String[] paramVals_arr = new String[] {};
    //
    //        paramNames.add( "user_id" );
    //        paramVals.add( userid );
    //        if ( text != null && !text.equals( "" ) )
    //        {
    //            paramNames.add( "text" );
    //            paramVals.add( text );
    //        }
    //        if ( tags != null && !tags.equals( "" ) )
    //        {
    //            paramNames.add( "tags" );
    //            paramVals.add( tags );
    //        }
    //        if ( userid != null && !userid.equals( "" ) )
    //        {
    //            paramNames.add( "userid" );
    //            paramVals.add( userid );
    //        }
    //        if ( per_page > 0 )
    //        {
    //            paramNames.add( "per_page" );
    //            paramVals.add( String.valueOf( per_page ) );
    //        }
    //        if ( page > 0 )
    //        {
    //            paramNames.add( "page" );
    //            paramVals.add( String.valueOf( page ) );
    //        }
    //
    //        return RestClient.CallFunction( "flickr.photos.search", paramNames.toArray( paramNames_arr ),
    //                                        paramVals.toArray( paramVals_arr ) );
    //    }
    //
    //    public static JSONObject photosGetInfo( String photoid )
    //    {
    //        return RestClient.CallFunction( "flickr.photos.getInfo", new String[] { "photo_id" }, new String[] { photoid } );
    //    }
    //
    //    public static JSONObject photosGetExif( String photoid )
    //    {
    //        return RestClient.CallFunction( "flickr.photos.getExif", new String[] { "photo_id" }, new String[] { photoid } );
    //    }
    //
    //    public static JSONObject photosGetSizes( String photoid )
    //    {
    //        return RestClient
    //            .CallFunction( "flickr.photos.getSizes", new String[] { "photo_id" }, new String[] { photoid } );
    //    }
    //
    //    public static JSONObject photosGetAllContexts( String photoid )
    //    {
    //        return RestClient.CallFunction( "flickr.photos.getAllContexts", new String[] { "photo_id" },
    //                                        new String[] { photoid } );
    //    }
    //

    //
    //    public static JSONObject tagsGetListUser( String userid )
    //    {
    //        return RestClient.CallFunction( "flickr.tags.getListUser", new String[] { "user_id" }, new String[] { userid } );
    //    }
    //
    //    public static JSONObject groupsGetInfo( String groupid )
    //    {
    //        return RestClient.CallFunction( "flickr.groups.getInfo", new String[] { "group_id" }, new String[] { groupid } );
    //    }
    //
    //    public static JSONObject groupsPoolsGetGroups()
    //    {
    //        return RestClient.CallFunction( "flickr.groups.pools.getGroups", null, null );
    //    }
    //
    //    public static JSONObject groupsPoolsGetPhotos( String groupid )
    //    {
    //        return APICalls.groupsPoolsGetPhotos( groupid, 0, 0 );
    //    }
    //
    //    public static JSONObject groupsPoolsGetPhotos( String groupid, int page )
    //    {
    //        return APICalls.groupsPoolsGetPhotos( groupid, page, 0 );
    //    }
    //
    //    public static JSONObject groupsPoolsGetPhotos( String groupid, int page, int per_page )
    //    {
    //        ArrayList<String> paramNames = new ArrayList<String>();
    //        ArrayList<String> paramVals = new ArrayList<String>();
    //        String[] paramNames_arr = new String[] {};
    //        String[] paramVals_arr = new String[] {};
    //
    //        paramNames.add( "group_id" );
    //        paramVals.add( groupid );
    //        if ( per_page > 0 )
    //        {
    //            paramNames.add( "per_page" );
    //            paramVals.add( String.valueOf( per_page ) );
    //        }
    //        if ( page > 0 )
    //        {
    //            paramNames.add( "page" );
    //            paramVals.add( String.valueOf( page ) );
    //        }
    //        return RestClient.CallFunction( "flickr.groups.pools.getPhotos", paramNames.toArray( paramNames_arr ),
    //                                        paramVals.toArray( paramVals_arr ) );
    //    }
    //
    //    public static JSONObject groupsSearch( String text, String perpage )
    //    {
    //        return RestClient.CallFunction( "flickr.groups.search", new String[] { "text", "per_page" }, new String[] {
    //            text,
    //            perpage } );
    //    }
    //
    //    public static String getGroupNameFromID( String groupid )
    //    {
    //        String name = "";
    //
    //        JSONObject group_info = groupsGetInfo( groupid );
    //        try
    //        {
    //            if ( group_info.has( "group" ) && group_info.getJSONObject( "group" ).has( "name" )
    //                && group_info.getJSONObject( "group" ).getJSONObject( "name" ).has( "_content" ) )
    //            {
    //                name = group_info.getJSONObject( "group" ).getJSONObject( "name" ).getString( "_content" );
    //            }
    //        }
    //        catch ( JSONException e )
    //        {
    //            e.printStackTrace();
    //        }
    //        return name;
    //    }
    //
    //    public static String[] getGroupInfoFromURL( String url )
    //    {
    //        String[] info = null;
    //
    //        JSONObject group_info = RestClient.CallFunction( "flickr.urls.lookupGroup", new String[] { "url" },
    //                                                         new String[] { url } );
    //        try
    //        {
    //            if ( group_info.has( "group" ) && group_info.getJSONObject( "group" ).has( "groupname" )
    //                && group_info.getJSONObject( "group" ).getJSONObject( "groupname" ).has( "_content" )
    //                && group_info.getJSONObject( "group" ).has( "id" ) )
    //            {
    //                String name = group_info.getJSONObject( "group" ).getJSONObject( "groupname" ).getString( "_content" );
    //                String id = group_info.getJSONObject( "group" ).getString( "id" );
    //                info = new String[] { name, id };
    //            }
    //        }
    //        catch ( JSONException e )
    //        {
    //            e.printStackTrace();
    //        }
    //        return info;
    //    }
    //
    //    public static String getPhotoNameFromID( String photoid )
    //    {
    //        String name = "";
    //
    //        JSONObject photo_info = photosGetInfo( photoid );
    //        try
    //        {
    //            if ( photo_info.has( "photo" ) && photo_info.getJSONObject( "photo" ).has( "title" )
    //                && photo_info.getJSONObject( "photo" ).getJSONObject( "title" ).has( "_content" ) )
    //            {
    //                name = photo_info.getJSONObject( "photo" ).getJSONObject( "title" ).getString( "_content" );
    //            }
    //        }
    //        catch ( JSONException e )
    //        {
    //            e.printStackTrace();
    //        }
    //        return name;
    //    }
    //

    //
    //    public static void favoritesAdd( String photoid )
    //    {
    //        RestClient.CallFunction( "flickr.favorites.add", new String[] { "photo_id" }, new String[] { photoid } );
    //    }
    //
    //    public static void favoritesRemove( String photoid )
    //    {
    //        RestClient.CallFunction( "flickr.favorites.remove", new String[] { "photo_id" }, new String[] { photoid } );
    //    }
    //
    //    public static JSONObject favoritesGetList( int page, int per_page )
    //    {
    //        return RestClient.CallFunction( "flickr.favorites.getList", new String[] { "page", "per_page" }, new String[] {
    //            String.valueOf( page ),
    //            String.valueOf( per_page ) } );
    //    }

}
