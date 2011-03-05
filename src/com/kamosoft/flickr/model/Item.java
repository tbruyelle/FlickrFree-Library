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
    String type;

    String id;

    String owner;

    String primary;

    String secret;

    String server;

    String commentsold;

    String commensnew;

    String views;

    String photos;

    String more;

    Title title;

    Activity activity;
}
