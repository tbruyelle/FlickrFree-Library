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
 * Model object for event Element
 * <event type="comment"
            user="12037949754@N01" username="Bees"
            dateadded="1144086424">yay</event>
 * @author tom
 * 
 */
public class Event
{
    public enum Type {
        comment, added_to_gallery, fave
    };

    private String type;

    private String user;

    private String username;

    private String dateadded;

    private String _content;

    private String galleryid;

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @return the user
     */
    public String getUser()
    {
        return user;
    }

    /**
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @return the dateadded
     */
    public String getDateadded()
    {
        return dateadded;
    }

    /**
     * @return the _content
     */
    public String get_content()
    {
        return _content;
    }

    /**
     * @return the galleryid
     */
    public String getGalleryid()
    {
        return galleryid;
    }
}
