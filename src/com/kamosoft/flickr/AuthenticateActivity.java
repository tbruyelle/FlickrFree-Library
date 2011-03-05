package com.kamosoft.flickr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.zmosoft.flickrfree.R;

public class AuthenticateActivity
    extends Activity
    implements OnClickListener
{

    SharedPreferences m_auth_prefs;

    String m_fail_msg;

    static final String TOKEN_INPUT_URL = "http://m.flickr.com/#/services/auth/";

    static final int DIALOG_ERR = 11;

    static final int DIALOG_HELP = 12;

    static final int DIALOG_ERR_HELP = 13;

    static final int DIALOG_NO_NETWORK = 14;

    static final public int AUTH_ERR = 23;

    static final public int AUTH_SUCCESS = 24;

    private class WebProgressTask
        extends AsyncTask<WebView, Integer, Object>
    {

        @Override
        protected Object doInBackground( WebView... params )
        {
            if ( params.length > 0 && params[0] != null )
            {
                WebView wv = (WebView) params[0];
                while ( wv.getProgress() < 100 )
                {
                    publishProgress( wv.getProgress() );
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute()
        {
            setProgress( Window.PROGRESS_START );
        }

        @Override
        protected void onProgressUpdate( Integer... values )
        {
            setProgress( Window.PROGRESS_END * values[0] / 100 );
        }

        @Override
        protected void onPostExecute( Object result )
        {
            setProgress( Window.PROGRESS_END );
        }

    }

    public static void registerAppParameters( Context context, String apiKey, String apiSecret, String authUrl )
    {
        SharedPreferences prefs = context.getSharedPreferences( "Auth", MODE_APPEND );
        Editor editor = prefs.edit();
        editor.putString( "api_key", apiKey );
        editor.putString( "api_secret", apiSecret );
        editor.putString( "auth_url", authUrl );
        editor.commit();
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_PROGRESS );

        m_auth_prefs = getSharedPreferences( "Auth", 0 );
        m_fail_msg = "";
        setResult( Activity.RESULT_CANCELED );

        setContentView( R.layout.authenticate );

        RestClient.setAuth( this );

        ( (Button) findViewById( R.id.btnAuthenticate ) ).setEnabled( checkAuthCode() );
        ( (Button) findViewById( R.id.btnAuthenticate ) ).setOnClickListener( this );
        ( (Button) findViewById( R.id.btnHelp ) ).setOnClickListener( this );

        ( (EditText) findViewById( R.id.authnum1 ) ).addTextChangedListener( new TextWatcher()
        {

            @Override
            public void afterTextChanged( Editable s )
            {
                if ( s.toString().length() == 3 )
                {
                    ( (EditText) findViewById( R.id.authnum2 ) ).requestFocus();
                }
                ( (Button) findViewById( R.id.btnAuthenticate ) ).setEnabled( checkAuthCode() );
            }

            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after )
            {
            }

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count )
            {
            }

        } );

        ( (EditText) findViewById( R.id.authnum2 ) ).addTextChangedListener( new TextWatcher()
        {

            @Override
            public void afterTextChanged( Editable s )
            {
                if ( s.toString().length() == 3 )
                {
                    ( (EditText) findViewById( R.id.authnum3 ) ).requestFocus();
                }
                ( (Button) findViewById( R.id.btnAuthenticate ) ).setEnabled( checkAuthCode() );
            }

            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after )
            {
            }

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count )
            {
            }

        } );

        ( (EditText) findViewById( R.id.authnum3 ) ).addTextChangedListener( new TextWatcher()
        {

            @Override
            public void afterTextChanged( Editable s )
            {
                ( (Button) findViewById( R.id.btnAuthenticate ) ).setEnabled( checkAuthCode() );
            }

            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after )
            {
            }

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count )
            {
            }

        } );
        SharedPreferences auth_prefs = getSharedPreferences( "Auth", 0 );
        if ( !auth_prefs.contains( "HasBeenRun" ) )
        {
            SharedPreferences.Editor auth_prefs_editor = auth_prefs.edit();
            auth_prefs_editor.putBoolean( "HasBeenRun", true );
            auth_prefs_editor.commit();
            showDialog( DIALOG_HELP );
        }

        if ( GlobalResources.CheckNetwork( this ) )
        {
            loadAuthPage();
        }
        else
        {
            showDialog( DIALOG_NO_NETWORK );
        }
    }

    private void loadAuthPage()
    {
        WebView wv = ( (WebView) findViewById( R.id.AuthWeb ) );
        CookieSyncManager.createInstance( this );
        CookieManager cookies = CookieManager.getInstance();
        cookies.removeAllCookie();
        wv.getSettings().setJavaScriptEnabled( true );
        wv.getSettings().setSavePassword( false );

        wv.setWebViewClient( new WebViewClient()
        {

            @Override
            public void onPageFinished( WebView view, String url )
            {
                // Resize text entry boxes to fix the screwed-up password entry
                // box. This is a bit of a hack, but it seems to work.
                // TODO: Test code on 2.2 and 1.6; this may not be necessary on
                // those OS versions.
                view.loadUrl( "javascript:" + "var inputCollection = document.getElementsByTagName(\"input\");"
                    + "for (var i=0; i<inputCollection.length; i++) {"
                    + "    inputCollection[i].style.height = '36px';"
                    + "    inputCollection[i].style.fontSize = '14px';" + "}" );
                if ( url.equals( TOKEN_INPUT_URL ) )
                {
                    ( (LinearLayout) findViewById( R.id.TokenInputLayout ) ).setVisibility( View.VISIBLE );
                    ( (EditText) findViewById( R.id.authnum1 ) ).requestFocus();
                    view.loadUrl( "javascript:(function() {\n" + "window.scrollTo(window.screen.height, 0);\n"
                        + "})()\n" );
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading( WebView view, String url )
            {
                view.loadUrl( url );
                return true;
            }
        } );
        String authUrl = m_auth_prefs.getString( "auth_url", "" );
        wv.loadUrl( authUrl );
        new WebProgressTask().execute( ( (WebView) findViewById( R.id.AuthWeb ) ) );
    }

    public boolean checkAuthCode()
    {
        return ( ( (EditText) findViewById( R.id.authnum1 ) ).getText().toString().length() == 3
            && ( (EditText) findViewById( R.id.authnum2 ) ).getText().toString().length() == 3 && ( (EditText) findViewById( R.id.authnum3 ) )
            .getText().toString().length() == 3 );
    }

    public void onClick( View v )
    {
        if ( v.getId() == R.id.btnAuthenticate )
        {
            String miniToken;
            miniToken = ( (EditText) findViewById( R.id.authnum1 ) ).getText().toString() + "-"
                + ( (EditText) findViewById( R.id.authnum2 ) ).getText().toString() + "-"
                + ( (EditText) findViewById( R.id.authnum3 ) ).getText().toString();

            JSONObject json_obj = APICalls.getFullToken( miniToken );
            try
            {
                // Check that authentication was successful
                if ( json_obj.getString( "stat" ).equals( "ok" ) )
                {
                    // Retrieve the username and fullname from the object.
                    String username = JSONParser.getString( json_obj, "auth/user/username" );
                    String fullname = JSONParser.getString( json_obj, "auth/user/fullname" );

                    // Get the "Auth" Shared preferences object to save authentication information
                    m_auth_prefs = getSharedPreferences( "Auth", 0 );

                    // Get the editor for auth_prefs
                    SharedPreferences.Editor auth_prefs_editor = m_auth_prefs.edit();

                    // Save all of the current authentication information. This will be the default account
                    // the next time the app is started.
                    auth_prefs_editor.putString( "full_token", JSONParser.getString( json_obj, "auth/token/_content" ) );
                    auth_prefs_editor.putString( "perms", JSONParser.getString( json_obj, "auth/perms/_content" ) );
                    auth_prefs_editor.putString( "nsid", JSONParser.getString( json_obj, "auth/user/nsid" ) );
                    auth_prefs_editor.putString( "username", username );
                    auth_prefs_editor.putString( "realname", fullname );
                    auth_prefs_editor.putString( "displayname", fullname.equals( "" ) ? username : fullname + " ("
                        + username + ")" );

                    // Save the entire JSON Authentication object under the username so it can be retrieved
                    // when switching accounts.
                    auth_prefs_editor.putString( "FlickrUsername_" + username, json_obj.toString() );

                    // Attempt to save all changes to Shared Preferences. If successful, set result to RESULT_OK.
                    if ( auth_prefs_editor.commit() )
                    {
                        setResult( Activity.RESULT_OK );
                    }
                    setResult( AUTH_SUCCESS );
                    finish();
                }
                else
                {
                    m_fail_msg = JSONParser.getString( json_obj, "message" );
                    if ( m_fail_msg == null )
                    {
                        m_fail_msg = "Unknown Error";
                    }
                    showDialog( DIALOG_ERR );
                }
            }
            catch ( JSONException e )
            {
                e.printStackTrace();
            }
        }
        else if ( v.getId() == R.id.btnHelp )
        {
            showDialog( DIALOG_HELP );
        }
    }

    protected Dialog onCreateDialog( int id )
    {
        Dialog err_dialog = null;

        AlertDialog.Builder builder;
        switch ( id )
        {
            case DIALOG_ERR:
                builder = new AlertDialog.Builder( this );
                builder.setMessage( m_fail_msg ).setTitle( R.string.ttlerror )
                    .setIcon( android.R.drawable.ic_dialog_alert )
                    .setPositiveButton( "Help", new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface dialog, int id )
                        {
                            m_fail_msg = "";
                            showDialog( DIALOG_ERR_HELP );
                        }
                    } ).setNegativeButton( "Close", new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface dialog, int id )
                        {
                            m_fail_msg = "";
                            AuthenticateActivity.this.setResult( AUTH_ERR );
                            AuthenticateActivity.this.finish();
                        }
                    } );
                err_dialog = builder.create();
                break;
            case DIALOG_ERR_HELP:
                builder = new AlertDialog.Builder( this );
                builder.setMessage( R.string.msgauthhelp ).setTitle( R.string.ttlhelp )
                    .setIcon( android.R.drawable.ic_dialog_info )
                    .setPositiveButton( "OK", new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface dialog, int id )
                        {
                            startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( GlobalResources.m_EDITPERMS_URL ) ) );
                        }
                    } ).setNegativeButton( "Cancel", new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface dialog, int id )
                        {
                            AuthenticateActivity.this.finish();
                        }
                    } );
                err_dialog = builder.create();
                break;
            case DIALOG_HELP:
                AssetManager assetManager = getAssets();
                InputStream stream = null;
                String help_text = "";
                try
                {
                    stream = assetManager.open( "authenticate_help.html" );
                    if ( stream != null )
                    {
                        byte[] buffer;
                        int result = 0;
                        while ( result >= 0 )
                        {
                            buffer = new byte[256];
                            result = stream.read( buffer );
                            help_text += new String( buffer );
                        }
                    }
                    stream.close();

                    builder = new AlertDialog.Builder( this );
                    LayoutInflater inflater = (LayoutInflater) this.getSystemService( LAYOUT_INFLATER_SERVICE );
                    View layout = inflater.inflate( R.layout.auth_help_dialog_layout, null );
                    builder.setView( layout );
                    builder.setTitle( R.string.ttlhelp ).setIcon( android.R.drawable.ic_dialog_info );

                    Button btn_ok = (Button) layout.findViewById( R.id.BtnOK );
                    btn_ok.setOnClickListener( new View.OnClickListener()
                    {
                        public void onClick( View v )
                        {
                            dismissDialog( DIALOG_HELP );
                        }
                    } );

                    // Replace all instances of "{AppName}" in help_text with the actual
                    // app name.
                    String app_name = getResources().getString( R.string.app_name );
                    String placeholder = "{AppName}";
                    String part_a, part_b;
                    int pos = help_text.indexOf( placeholder );
                    while ( pos >= 0 )
                    {
                        part_a = help_text.substring( 0, pos );
                        part_b = help_text.substring( pos + placeholder.length() );
                        help_text = part_a + app_name + part_b;
                        pos = help_text.indexOf( placeholder );
                    }

                    WebView help_text_view = (WebView) layout.findViewById( R.id.AuthHelpInfo );
                    help_text_view.loadData( help_text, "text/html", "utf-8" );
                    err_dialog = builder.create();
                }
                catch ( IOException e )
                {
                }
                break;
            case DIALOG_NO_NETWORK:
                builder = new AlertDialog.Builder( this );
                builder.setMessage( R.string.msgnnetworkerror ).setTitle( R.string.ttlerror )
                    .setIcon( android.R.drawable.ic_dialog_alert )
                    .setNeutralButton( "OK", new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface dialog, int id )
                        {
                            dialog.dismiss();
                        }
                    } );
                err_dialog = builder.create();
                break;
        }

        return err_dialog;
    }

    public static boolean SetActiveUser( SharedPreferences prefs, String username )
    {
        return AuthenticateActivity.SetActiveUser( prefs, username, true );
    }

    public static boolean IsLoggedIn( Context context )
    {
        String token = ( (SharedPreferences) context.getSharedPreferences( "Auth", 0 ) ).getString( "full_token", "" );

        return ( !token.equals( "" ) );
    }

    public static boolean SetActiveUser( SharedPreferences prefs, String username, boolean logout_if_invalid )
    {
        try
        {
            SharedPreferences.Editor prefs_editor = prefs.edit();
            String user_obj_str = username.equals( "" ) ? "" : prefs.getString( "FlickrUsername_" + username, "" );

            if ( user_obj_str.equals( "" ) )
            {
                if ( logout_if_invalid )
                {
                    AuthenticateActivity.LogOut( prefs );
                }
                return false;
            }
            else
            {
                JSONObject user_obj = new JSONObject( user_obj_str );

                // Retrieve the full name from the object.
                String fullname = user_obj.getJSONObject( "auth" ).getJSONObject( "user" ).getString( "fullname" );

                // Save all of the current authentication information. This will be the default account
                // the next time the app is started.
                prefs_editor.putString( "full_token", JSONParser.getString( user_obj, "auth/token/_content" ) );
                prefs_editor.putString( "perms", JSONParser.getString( user_obj, "auth/perms/_content" ) );
                prefs_editor.putString( "nsid", JSONParser.getString( user_obj, "auth/user/nsid" ) );
                prefs_editor.putString( "username", username );
                prefs_editor.putString( "realname", fullname );
                prefs_editor.putString( "displayname", fullname.equals( "" ) ? username : fullname + " (" + username
                    + ")" );
                prefs_editor.commit();
            }
        }
        catch ( JSONException e )
        {
            e.printStackTrace();
        }
        return true;
    }

    public static void RemoveUser( SharedPreferences prefs, String username )
    {
        // Get the editor for prefs
        SharedPreferences.Editor prefs_editor = prefs.edit();

        prefs_editor.remove( "FlickrUsername_" + username );
        prefs_editor.commit();
        if ( prefs.getString( "username", "" ).equals( username ) )
        {
            AuthenticateActivity.LogOut( prefs );
        }
    }

    public static void LogOut( SharedPreferences prefs )
    {
        // Get the editor for prefs
        SharedPreferences.Editor prefs_editor = prefs.edit();

        prefs_editor.remove( "full_token" );
        prefs_editor.remove( "perms" );
        prefs_editor.remove( "nsid" );
        prefs_editor.remove( "username" );
        prefs_editor.remove( "realname" );
        prefs_editor.remove( "displayname" );
        prefs_editor.commit();
    }

    public static void ExportAuth( SharedPreferences auth_prefs, String path )
    {
        Map<String, ?> m = auth_prefs.getAll();
        try
        {
            File f = new File( path );
            if ( !f.exists() )
            {
                f.createNewFile();
            }
            FileOutputStream of = new FileOutputStream( f );
            for ( String key : m.keySet() )
            {
                if ( key.contains( "FlickrUsername_" ) )
                {
                    new PrintStream( of ).println( key + " : " + m.get( key ).toString() );
                }
            }
            of.close();
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    public static void ImportAuth( SharedPreferences auth_prefs, String path )
    {
        try
        {
            BufferedReader read_buf = new BufferedReader( new FileReader( path ) );
            SharedPreferences.Editor auth_prefs_edit = auth_prefs.edit();
            String s = read_buf.readLine();
            String[] parsed = null;
            while ( s != null )
            {
                parsed = s.split( " : ", 2 );
                if ( parsed.length == 2 && parsed[0].contains( "FlickrUsername_" ) )
                {
                    auth_prefs_edit.putString( parsed[0], parsed[1] );
                }
                s = read_buf.readLine();
            }
            auth_prefs_edit.commit();
            read_buf.close();
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}
