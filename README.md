FlickrFree Library
==================

FlickrFree Library is an Android library which aims to provide access to the Flickr API.

It has been forked from [FlickrFree project](http://code.google.com/p/flickrfree)

_Still in alpha phase_

Authentification
----------------------

To authenticate you need to : 

1. Push your flickr application keys to the library

        AuthenticateActivity.registerAppParameters( this, api_key, api_secret, auth_url );
        RestClient.setAuth( this ); // this call will become unnecessary in next versions

2. Start the activity AuthenticateActivity

         startActivityForResult( new Intent( this, AuthenticateActivity.class ), AUTHENTICATE );

3. Override the onActivityResult to retrieve the authentication result.

        @Override
        protected void onActivityResult( int requestCode, int resultCode, Intent data )
        {
        super.onActivityResult( requestCode, resultCode, data );
        switch ( requestCode )
        {
            case AUTHENTICATE:
                if ( resultCode == AuthenticateActivity.AUTH_SUCCESS )
                {                    
                    RestClient.setAuth( this ); // this call will become unnecessary in next versions
                    Toast.makeText( this, "Auth OK !", Toast.LENGTH_SHORT ).show();
                }
                else
                {
                    Toast.makeText( this, "Auth KO !", Toast.LENGTH_SHORT ).show();
                }
                break;
         }
         }

Available flickr methods
---------------------------

* <pre>flickr.activity.userPhotos</pre> with <pre>APICalls.getActivityUserPhotos( String userId )</pre>
* <pre>flickr.photos.getInfo</pre> with <pre>APICalls.getPhotoInfo( String photoId )</pre>