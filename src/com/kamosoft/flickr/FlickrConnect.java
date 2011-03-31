/**
 * Copyright 2011 kamosoft
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kamosoft.flickr;

import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.kamosoft.flickr.model.FlickrApiResult;

/**
 * @author Tom
 * created 23 mars 2011
 */
public class FlickrConnect
{
    private static String PREF_APP_NAME = "app_name";

    private static String PREF_API_KEY = "api_key";

    private static String PREF_API_SECRET = "api_secret";

    private static String PREF_AUTH_URL = "auth_url";

    private static String PREF_FULL_TOKEN = "full_token";

    private static final String PREF_PERMS = "perms";

    private static final String PREF_USERID = "nsid";

    private static final String PREF_USERNAME = "username";

    private static final String PREF_REALNAME = "realname";

    private static final String PREF_DISPLAYNAME = "displayname";

    static final public int AUTH_ERR = 23;

    static final public int AUTH_SUCCESS = 24;

    private Context mContext;

    private FlickrParameters mFlickrParameters;

    public FlickrConnect( Context context )
    {
        mContext = context;
    }

    public FlickrParameters getFlickrParameters()
    {
        if ( mFlickrParameters == null )
        {
            mFlickrParameters = loadFlickrParameters( mContext );
        }
        return mFlickrParameters;
    }

    public static void registerFlickrParameters( Context context, FlickrParameters flickrParameters )
    {
        SharedPreferences prefs = context.getSharedPreferences( GlobalResources.PREFERENCES_ID, 0 );
        Editor editor = prefs.edit();
        editor.putString( PREF_APP_NAME, flickrParameters.getAppName() );
        editor.putString( PREF_API_KEY, flickrParameters.getApiKey() );
        editor.putString( PREF_API_SECRET, flickrParameters.getApiSecret() );
        editor.putString( PREF_AUTH_URL, flickrParameters.getAuthUrl() );
        editor.putString( PREF_FULL_TOKEN, flickrParameters.getFullToken() );
        editor.putString( PREF_PERMS, flickrParameters.getPerms() );
        editor.putString( PREF_USERID, flickrParameters.getNsid() );
        editor.putString( PREF_USERNAME, flickrParameters.getUserName() );
        editor.putString( PREF_DISPLAYNAME, flickrParameters.getDisplayName() );
        editor.putString( PREF_REALNAME, flickrParameters.getRealName() );
        editor.commit();
    }

    public static void clearFlickrParameters( Context context )
    {
        SharedPreferences prefs = context.getSharedPreferences( GlobalResources.PREFERENCES_ID, 0 );
        Editor editor = prefs.edit();
        editor.remove( PREF_APP_NAME );
        editor.remove( PREF_API_KEY );
        editor.remove( PREF_API_SECRET );
        editor.remove( PREF_AUTH_URL );
        editor.remove( PREF_FULL_TOKEN );
        editor.remove( PREF_PERMS );
        editor.remove( PREF_USERID );
        editor.remove( PREF_USERNAME );
        editor.remove( PREF_DISPLAYNAME );
        editor.remove( PREF_REALNAME );
        editor.commit();
    }

    public static FlickrParameters loadFlickrParameters( Context context )
    {
        SharedPreferences prefs = context.getSharedPreferences( GlobalResources.PREFERENCES_ID, 0 );
        String appName = prefs.getString( PREF_APP_NAME, null );
        if ( appName == null )
        {
            return null;
        }
        FlickrParameters flickrParameters = new FlickrParameters( appName );
        flickrParameters.setApiKey( prefs.getString( PREF_API_KEY, null ) );
        flickrParameters.setApiSecret( prefs.getString( PREF_API_SECRET, null ) );
        flickrParameters.setAuthUrl( prefs.getString( PREF_AUTH_URL, null ) );
        flickrParameters.setFullToken( prefs.getString( PREF_FULL_TOKEN, null ) );
        flickrParameters.setPerms( prefs.getString( PREF_PERMS, null ) );
        flickrParameters.setNsid( prefs.getString( PREF_USERID, null ) );
        flickrParameters.setUserName( prefs.getString( PREF_USERNAME, null ) );
        flickrParameters.setDisplayName( prefs.getString( PREF_DISPLAYNAME, null ) );
        flickrParameters.setRealName( prefs.getString( PREF_REALNAME, null ) );
        return flickrParameters;
    }

    /**
     * @param activity
     * @param requestCode
     */
    public void authorize( Activity activity, String appName, String apiKey, String apiSecret, String authUrl,
                           int requestCode )
    {
        Intent intent = new Intent( activity, AuthenticateActivity.class );
        FlickrParameters flickrParameters = new FlickrParameters( appName );
        flickrParameters.setApiKey( apiKey );
        flickrParameters.setApiSecret( apiSecret );
        flickrParameters.setAuthUrl( authUrl );
        //registerFlickrParameters( activity, flickrParameters );
        intent.putExtra( "FlickParams", flickrParameters );
        activity.startActivityForResult( intent, requestCode );
    }

    /**
     * @param methodName
     * @param paramNames
     * @param paramValues
     * @param authenticated
     * @return
     */
    private FlickrApiResult callApi( String methodName, String[] paramNames, String[] paramValues, boolean authenticated )
        throws IOException
    {
        try
        {
            return APICalls.perform( methodName, paramNames, paramValues, getFlickrParameters(), authenticated );
        }
        catch ( IOException e )
        {
            Log.e( "IOException during call " + methodName );
            Log.e( e.getMessage(), e );
            throw e;
        }
    }

    /**
     * Retrieve the last user photo activity
     * @param userId
     * @param timeFrame
     * @param perPage
     * @param page
     * @return FlickrApiResult
     */
    public FlickrApiResult getActivityUserPhotos( String userId, String timeFrame, String perPage, String page )
        throws IOException
    {
        return callApi( "flickr.activity.userPhotos", new String[] { "user_id", "timeframe", "per_page", "page" },
                        new String[] { userId, timeFrame, perPage, page }, true );
    }

    /**
     * Retrieve the last user comment activity
     * @param userId
     * @param perPage
     * @param page
     * @return FlickrApiResult
     */
    public FlickrApiResult getActivityUserComments( String userId, String perPage, String page )
        throws IOException
    {
        return callApi( "flickr.activity.userComments", new String[] { "user_id", "per_page", "page" }, new String[] {
            userId,
            perPage,
            page }, true );
    }

    /**
     * Retrieve photo information
     * @param photoId
     * @return FlickrApiResult
     */
    public FlickrApiResult getPhotoInfo( String photoId )
        throws IOException
    {
        return callApi( "flickr.photos.getInfo", new String[] { "photo_id" }, new String[] { photoId }, true );
    }

    /**
     * synchronous method for check if logged in
     * @return
     */
    public boolean isLoggedIn()
        throws IOException
    {
        if ( getFlickrParameters() == null || getFlickrParameters().getFullToken() == null )
        {
            return false;
        }
        if ( !APICalls.ping( getFlickrParameters() ) )
        {
            return false;
        }
        FlickrApiResult result = callApi( "flickr.auth.checkToken", null, null, true );
        return result != null && result.isStatusOk();
    }

    /**
     * Asynchronous method for check if logged in
     * @param loginHandler
     * @return
     */
    public void isLoggedIn( final LoginHandler loginHandler )
    {
        new AsyncTask<Void, Void, Integer>()
        {
            private Dialog mDialog;

            private Integer LOGIN_SUCCESS = Integer.valueOf( 0 );

            private Integer LOGIN_FAILED = Integer.valueOf( 1 );

            private Integer LOGIN_ERROR = Integer.valueOf( 2 );

            /**
             * @see android.os.AsyncTask#onPreExecute()
             */
            @Override
            protected void onPreExecute()
            {
                mDialog = ProgressDialog.show( mContext, "", "Checking if logged in...", true );
            }

            @Override
            protected Integer doInBackground( Void... params )
            {
                try
                {
                    return FlickrConnect.this.isLoggedIn() ? LOGIN_SUCCESS : LOGIN_FAILED;
                }
                catch ( IOException e )
                {
                    return LOGIN_ERROR;
                }
            }

            /**
             * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
             */
            @Override
            protected void onPostExecute( Integer result )
            {
                mDialog.dismiss();

                if ( result.equals( LOGIN_SUCCESS ) )
                {
                    loginHandler.onLoginSuccess();
                }
                else if ( result.equals( LOGIN_FAILED ) )
                {
                    loginHandler.onLoginFailed();
                }
                else
                {
                    loginHandler.onLoginError();
                }
            }
        }.execute();

    }

    public interface LoginHandler
    {
        void onLoginSuccess();

        void onLoginFailed();

        void onLoginError();
    }

    public void logOut()
    {
        logOut( true );
    }

    /**
     * @param fromYahoo if true also remove the cookies to force a yahoo login again
     */
    public void logOut( boolean fromYahoo )
    {
        if ( fromYahoo )
        {
            CookieSyncManager.createInstance( mContext );
            CookieManager cookies = CookieManager.getInstance();
            cookies.removeAllCookie();
        }
        clearFlickrParameters( mContext );
    }
}
