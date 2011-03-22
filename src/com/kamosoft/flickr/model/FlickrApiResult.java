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
package com.kamosoft.flickr.model;

import com.kamosoft.flickr.Log;

/**
 * Model object for the root element
 * @author tom
 */
public class FlickrApiResult
{
    private String stat;

    private Auth auth;

    private Items items;

    private Photo photo;

    private String code;

    private String message;

    public enum Status {
        ok, fail
    };

    /**
     * @return true if the response status is OK
     */
    public boolean isStatusOk()
    {
        Log.d( "isStatusOk ? " + stat );
        if ( stat == null )
        {

            return false;
        }
        return stat.equals( Status.ok.name() );
    }

    /**
     * @return the code
     */
    public String getCode()
    {
        return code;
    }

    /**
     * @return the message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @return the auth
     */
    public Auth getAuth()
    {
        return auth;
    }

    /**
     * @return the items
     */
    public Items getItems()
    {
        return items;
    }

    /**
     * @return the photo
     */
    public Photo getPhoto()
    {
        return photo;
    }

    /**
     * @return the stat
     */
    public String getStat()
    {
        return stat;
    }

}
