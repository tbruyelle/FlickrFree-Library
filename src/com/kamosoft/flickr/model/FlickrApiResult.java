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

/**
 * Model object for the root element
 * @author tom
 */
public class FlickrApiResult
{
    private Items items;

    private Photo photo;

    private String stat;

    public enum Status {
        ok, fail
    };

    /**
     * @return true if the response status is OK
     */
    public boolean isStatusOk()
    {
        if ( stat == null )
        {
            return false;
        }
        return stat.equals( Status.ok );
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
