package com.kamosoft.flickr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.kamosoft.flickr.model.FlickrApiResult;

public class AuthenticateActivity
    extends Activity
    implements OnClickListener
{
    private FlickrParameters mFlickrParameters;

    private String mFailMessage;

    static final String TOKEN_INPUT_URL = "http://m.flickr.com/#/services/auth/";

    private static final int DIALOG_ERR = 11;

    private static final int DIALOG_HELP = 12;

    private static final int DIALOG_ERR_HELP = 13;

    private static final int DIALOG_NO_NETWORK = 14;

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
            Log.d( "progress update : " + values[0] );
            setProgress( Window.PROGRESS_END * values[0] / 100 );
        }

        @Override
        protected void onPostExecute( Object result )
        {
            setProgress( Window.PROGRESS_END );
        }

    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_PROGRESS );

        mFlickrParameters = (FlickrParameters) getIntent().getSerializableExtra( "FlickParams" );

        mFailMessage = "";
        setResult( Activity.RESULT_CANCELED );

        setContentView( R.layout.authenticate );

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

        SharedPreferences authPrefs = getSharedPreferences( GlobalResources.PREFERENCES_ID, 0 );
        if ( !authPrefs.contains( GlobalResources.PREF_HASBEENRUN ) )
        {
            SharedPreferences.Editor auth_prefs_editor = authPrefs.edit();
            auth_prefs_editor.putBoolean( GlobalResources.PREF_HASBEENRUN, true );
            auth_prefs_editor.commit();
            showDialog( DIALOG_HELP );
        }

        new CheckNetworkTask().execute();
    }

    /**
     * use asyncTask to avoid ANR
     * @author Tom
     * created 17 mars 2011
     */
    private class CheckNetworkTask
        extends AsyncTask<Void, Void, Boolean>
    {
        private Dialog mDialog;

        /**
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute()
        {
            // FIXME why getString throw ResourceNotFoundExeption ? very very weird!
            //            mDialog = ProgressDialog.show( AuthenticateActivity.this, "",
            //                                           AuthenticateActivity.this.( R.string.checking_network ), true );
            mDialog = ProgressDialog.show( AuthenticateActivity.this, "", "Checking network, please wait...", true );
        }

        /**
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Boolean doInBackground( Void... params )
        {
            return APICalls.checkNetwork( AuthenticateActivity.this, mFlickrParameters );
        }

        /**
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute( Boolean result )
        {
            mDialog.dismiss();
            if ( result.booleanValue() )
            {
                Log.d( "AuthenticateActivity: network OK" );
                loadAuthPage();
            }
            else
            {
                Log.e( "AuthenticateActivity: No network" );
                AuthenticateActivity.this.showDialog( DIALOG_NO_NETWORK );
            }
        }
    }

    private void loadAuthPage()
    {
        WebView wv = ( (WebView) findViewById( R.id.AuthWeb ) );
        //        CookieSyncManager.createInstance( this );
        //        CookieManager cookies = CookieManager.getInstance();
        //        cookies.removeAllCookie();
        wv.getSettings().setJavaScriptEnabled( true );
        //        wv.getSettings().setSavePassword( false );

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
        wv.loadUrl( mFlickrParameters.getAuthUrl() );
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

            FlickrApiResult flickrApiResult;
            try
            {
                flickrApiResult = APICalls.getFullToken( miniToken, mFlickrParameters );

                // Check that authentication was successful
                if ( flickrApiResult.isStatusOk() )
                {
                    Log.d( "Full token retrieved " + flickrApiResult.getAuth().getToken() );
                    // Retrieve the username and fullname from the object.
                    String username = flickrApiResult.getAuth().getUser().getUsername();
                    String fullname = flickrApiResult.getAuth().getUser().getFullname();

                    // Save all of the current authentication information. This will be the default account
                    // the next time the app is started.
                    mFlickrParameters.setFullToken( flickrApiResult.getAuth().getToken().getContent() );
                    mFlickrParameters.setPerms( flickrApiResult.getAuth().getPerms().getContent() );
                    mFlickrParameters.setNsid( flickrApiResult.getAuth().getUser().getNsid() );
                    mFlickrParameters.setUserName( username );
                    mFlickrParameters.setRealName( fullname );
                    mFlickrParameters.setDisplayName( fullname.equals( "" ) ? username : fullname + " (" + username
                        + ")" );
                    FlickrConnect.registerFlickrParameters( this, mFlickrParameters );

                    setResult( FlickrConnect.AUTH_SUCCESS );
                    finish();
                }
                else
                {
                    mFailMessage = flickrApiResult.getMessage();
                    if ( mFailMessage == null )
                    {
                        mFailMessage = "Unknown Error";
                    }
                    showDialog( DIALOG_ERR );
                }
            }
            catch ( IOException e )
            {
                mFailMessage = e.getMessage();
                showDialog( DIALOG_ERR );
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
                builder.setMessage( mFailMessage ).setTitle( R.string.ttlerror )
                    .setIcon( android.R.drawable.ic_dialog_alert )
                    .setPositiveButton( "Help", new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface dialog, int id )
                        {
                            mFailMessage = "";
                            showDialog( DIALOG_ERR_HELP );
                        }
                    } ).setNegativeButton( "Close", new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface dialog, int id )
                        {
                            mFailMessage = "";
                            AuthenticateActivity.this.setResult( FlickrConnect.AUTH_ERR );
                            AuthenticateActivity.this.finish();
                        }
                    } );
                err_dialog = builder.create();
                break;
            case DIALOG_ERR_HELP:
                builder = new AlertDialog.Builder( this );
                String appName = mFlickrParameters.getAppName();
                builder.setMessage( getString( R.string.msgauthhelp, appName, appName ) ).setTitle( R.string.ttlhelp )
                    .setIcon( android.R.drawable.ic_dialog_info )
                    .setPositiveButton( "OK", new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface dialog, int id )
                        {
                            startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( APICalls.m_EDITPERMS_URL ) ) );
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

                try
                {
                    String help_text = getFileContent( getResources(), R.raw.authenticate_help );

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
                    Log.e( e.getMessage(), e );
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
                            AuthenticateActivity.this.setResult( FlickrConnect.AUTH_ERR );
                            AuthenticateActivity.this.finish();
                        }
                    } );
                err_dialog = builder.create();
                break;

            default:
                Log.e( "Unable to create dialog with id " + id );
        }

        return err_dialog;
    }

    public String getFileContent( Resources resources, int rawId )
        throws IOException
    {
        InputStream is = resources.openRawResource( rawId );

        InputStreamReader isr = new InputStreamReader( is, Charset.forName( "ISO-8859-1" ) );

        // We guarantee that the available method returns the total
        // size of the asset... of course, this does mean that a single
        // asset can't be more than 2 gigs.
        int size = is.available();

        // Read the entire asset into a local byte buffer.
        char[] buffer = new char[size];
        isr.read( buffer );
        isr.close();
        is.close();

        // Convert the buffer into a string.
        return new String( buffer );
    }

}
