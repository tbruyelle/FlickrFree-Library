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
 * Model object for the Item element
 * <item type="photoset" id="395" owner="12037949754@N01" 
        primary="6521" secret="5a3cc65d72" server="2" 
        commentsold="1" commentsnew="1"
        views="33" photos="7" more="0">
        <title>A set of photos</title>
        <activity>
            <event type="comment"
            user="12037949754@N01" username="Bees"
            dateadded="1144086424">yay</event>
        </activity>
    </item>
 * @author tom
 *
 */
public class Item
{
    public enum Type {
        photo
    };

    private String type;

    private String id;

    private String owner;

    private String primary;

    private String secret;

    private String server;

    private Title title;

    private Activity activity;

    private int comment;

    private String farm;

    private int notes;

    private int faves;

    private int views;

    /**
     * @return the type
     */
    public Type getType()
    {
        return Type.valueOf( type );
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @return the owner
     */
    public String getOwner()
    {
        return owner;
    }

    /**
     * @return the primary
     */
    public String getPrimary()
    {
        return primary;
    }

    /**
     * @return the secret
     */
    public String getSecret()
    {
        return secret;
    }

    /**
     * @return the server
     */
    public String getServer()
    {
        return server;
    }

    /**
     * @return the title
     */
    public Title getTitle()
    {
        return title;
    }

    /**
     * @return the activity
     */
    public Activity getActivity()
    {
        return activity;
    }

    /**
     * @return the comment
     */
    public int getComment()
    {
        return comment;
    }

    /**
     * @return the farm
     */
    public String getFarm()
    {
        return farm;
    }

    /**
     * @return the notes
     */
    public int getNotes()
    {
        return notes;
    }

    /**
     * @return the faves
     */
    public int getFaves()
    {
        return faves;
    }

    /**
     * @return the views
     */
    public int getViews()
    {
        return views;
    }

}
