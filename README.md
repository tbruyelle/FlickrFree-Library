FlickrFree Library
==================

FlickrFree Library is an Android library which aims to provide access to the Flickr API.

It has been forked from [FlickrFree project](http://code.google.com/p/flickrfree)


Installation using Eclipse
----------------------

1. Clone the library sources

        git clone  git@github.com:tbruyelle/FlickrFree-Library.git

2. Import the sources in Eclipse *File -> New -> Android Project*

3. Add the library in your project *properties -> Android -> Library -> Add -> FlickrFree-Library*

Authentication
----------------------

To authenticate you need to : 

1. Instantiate a `FlickrConnect` class

        FlickrConnect flickrConnect = new FlickrConnect( this );       

2. check if already logged in, if not, call the `authorize` method, and pass your Flickr app parameters. 

        /* check the authentification */
        if ( mFlickrConnect.IsLoggedIn() )
        {
            /* auth OK */
            (...)
        }
        else
        {
            /* auth need to be done */
            flickrConnect.authorize( this, getString( R.string.app_name ), 
                                                      getString( R.string.api_key ),
                                                      getString( R.string.api_secret ), 
                                                      getString( R.string.auth_url ), 
                                                      AUTHENTICATE ); 
        }
        

3. Override the `onActivityResult` to retrieve the authentication result.

        @Override
        protected void onActivityResult( int requestCode, int resultCode, Intent data )
        {
        super.onActivityResult( requestCode, resultCode, data );
        switch ( requestCode )
        {
            case AUTHENTICATE:
                if ( resultCode == FlickrConnect.AUTH_SUCCESS )
                {                    
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

* `flickr.auth.checkToken` with `flickrConnect.isLoggedIn()`
* `flickr.photos.getInfo` with `flickrConnect.getPhotoInfo( String photoId )`
* `flickr.activity.userPhotos` with `flickrConnect.getActivityUserPhotos( String userId, String timeFrame, String perPage, String page )`
* `flickr.activity.userComments` with `flickrConnect.getActivityUserComments( String userId, String perPage, String page )`