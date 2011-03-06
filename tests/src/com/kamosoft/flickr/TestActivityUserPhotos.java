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

import android.test.AndroidTestCase;

import com.kamosoft.flickr.model.JsonFlickrApi;

/**
 * @author tom
 */
public class TestActivityUserPhotos
    extends AndroidTestCase
{
    public void testActivityUserPhotos()
    {
        try
        {
            AuthenticateActivity.registerAppParameters( getContext(), "bb0a41d1bed19e407cc03f82d33d7bf7",
                                                        "d97a09b9fa80881b",
                                                        null );
            /* dont work because there is no token, need to authentify and there is no control on webview with the android junit */
            RestClient.setAuth( getContext() );
            JsonFlickrApi jsonFlickrApi = APICalls.getActivityUserPhotos( "9542950@N07", "5d", null, null );
            assertNotNull( jsonFlickrApi );
        }
        catch ( Exception e )
        {
            System.out.println( "Exception " + e.getClass().getName() + " : " + e.getMessage() );
            fail( e.getMessage() );
        }
    }

}
