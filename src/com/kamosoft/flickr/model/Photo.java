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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Model object for Photo element
 * @author tom
 */
public class Photo
{
    private String id;

    private String secret;

    private String server;

    private String farm;

    private String dateuploaded;

    private String isfavorite;

    private int safety_level;

    private int rotation;

    private int license;

    private String originalsecret;

    private String originalformat;

    private String media;

    private int views;

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
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
     * @return the farm
     */
    public String getFarm()
    {
        return farm;
    }

    /**
     * @return the dateuploaded
     */
    public String getDateuploaded()
    {
        return dateuploaded;
    }

    /**
     * @return the isfavorite
     */
    public String getIsfavorite()
    {
        return isfavorite;
    }

    /**
     * @return the safety_level
     */
    public int getSafety_level()
    {
        return safety_level;
    }

    /**
     * @return the rotation
     */
    public int getRotation()
    {
        return rotation;
    }

    /**
     * @return the license
     */
    public int getLicense()
    {
        return license;
    }

    /**
     * @return the originalsecret
     */
    public String getOriginalsecret()
    {
        return originalsecret;
    }

    /**
     * @return the originalformat
     */
    public String getOriginalformat()
    {
        return originalformat;
    }

    /**
     * @return the media
     */
    public String getMedia()
    {
        return media;
    }

    /**
     * @return the views
     */
    public int getViews()
    {
        return views;
    }

    public enum Size {
        SMALLSQUARE(0), THUMB(1), SMALL(2), MED(3), LARGE(4), ORIG(5);

        private int m_sizenum;

        private Size( int i )
        {
            m_sizenum = i;
        }

        public int getNum()
        {
            return m_sizenum;
        }

        public void setNum( int num )
        {
            m_sizenum = num;
        }

        public String toString()
        {
            if ( m_sizenum == 0 )
            {
                return "Small Square";
            }
            else if ( m_sizenum == 1 )
            {
                return "Thumb";
            }
            else if ( m_sizenum == 2 )
            {
                return "Small";
            }
            else if ( m_sizenum == 3 )
            {
                return "Medium";
            }
            else if ( m_sizenum == 4 )
            {
                return "Large";
            }
            else if ( m_sizenum == 5 )
            {
                return "Original";
            }
            else
            {
                return "Unknown";
            }
        }
    }

    public Bitmap getBitmap( Size size )
        throws JSONException, IOException
    {
        return getBitmapFromURL( getImageUrl( size ) );
    }

    private Bitmap getBitmapFromURL( String url )
        throws JSONException, IOException
    {
        Bitmap bm = null;
        URL aURL = new URL( url );
        URLConnection conn = aURL.openConnection();
        conn.connect();
        InputStream is = conn.getInputStream();
        BufferedInputStream bis = new BufferedInputStream( is );
        bm = BitmapFactory.decodeStream( bis );
        bis.close();
        is.close();

        return bm;
    }

    private String getImageUrl( Size size )
    {
        return getImageURL( getFarm(), getServer(), getId(), getSecret(), size, getOriginalformat() );
    }

    private String getImageURL( String farm, String server, String id, String secret, Size size, String extension )
    {
        String img_url = "http://farm" + farm + ".static.flickr.com/" + server + "/" + id + "_" + secret;
        if ( size == Size.SMALLSQUARE )
        {
            img_url = img_url + "_s";
        }
        else if ( size == Size.THUMB )
        {
            img_url = img_url + "_t";
        }
        else if ( size == Size.SMALL )
        {
            img_url = img_url + "_m";
        }
        else if ( size == Size.LARGE )
        {
            img_url = img_url + "_b";
        }
        else if ( size == Size.ORIG )
        {
            img_url = img_url + "_o";
        }
        img_url = img_url + "." + extension;

        return img_url;
    }

    //TODO to be completed
    /*
     * <photo id="687589192" secret="59e0fda23e" server="1026" farm="2" dateuploaded="1183324900" isfavorite="0" license="3" safety_level="0" rotation="0" originalsecret="026a7199d7" originalformat="jpg" views="119" media="photo">
    <owner nsid="9542950@N07" username="kosokund" realname="" location="" iconserver="5100" iconfarm="6"/>
    <title>S�ance de lavage</title>
    <description>Mon chat Gollum</description>
    <visibility ispublic="1" isfriend="0" isfamily="0"/>
    <dates posted="1183324900" taken="2006-05-26 19:36:10" takengranularity="0" lastupdate="1299441291"/>
    <permissions permcomment="3" permaddmeta="2"/>
    <editability cancomment="1" canaddmeta="1"/>
    <publiceditability cancomment="1" canaddmeta="0"/>
    <usage candownload="1" canblog="1" canprint="1" canshare="1"/>
    <comments>12</comments>
    <notes/>
    <tags>
    <tag id="9521620-687589192-3170" author="9542950@N07" raw="chat" machine_tag="0">chat</tag>
    <tag id="9521620-687589192-952" author="9542950@N07" raw="animal" machine_tag="0">animal</tag>
    <tag id="9521620-687589192-142906" author="9542950@N07" raw="mignon" machine_tag="0">mignon</tag>
    <tag id="9521620-687589192-559" author="9542950@N07" raw="cute" machine_tag="0">cute</tag>
    <tag id="9521620-687589192-1344" author="9542950@N07" raw="cat" machine_tag="0">cat</tag>
    <tag id="9521620-687589192-11258901" author="9542950@N07" raw="Thebiggestgroupwithonlycats" machine_tag="0">thebiggestgroupwithonlycats</tag>
    <tag id="9521620-687589192-6022323" author="9542950@N07" raw="kissablekat" machine_tag="0">kissablekat</tag>
    <tag id="9521620-687589192-6788663" author="9542950@N07" raw="bestofcats" machine_tag="0">bestofcats</tag>
    </tags>
    <location latitude="48.812515" longitude="2.302751" accuracy="14" context="0" place_id="2RJQF4.YA5ocj_dD3A" woeid="12648639">
    <locality place_id="2RJQF4.YA5ocj_dD3A" woeid="12648639">Malakoff</locality>
    <county place_id=".c1FvvuYBJyQGB9wUA" woeid="15022631">Hauts-de-Seine</county>
    <region place_id="Pyt7lMSeAJkMp.Xb" woeid="7153319">�le-de-France</region>
    <country place_id="6immEPubAphfvM5R0g" woeid="23424819">France</country>
    </location>
    <geoperms ispublic="1" iscontact="0" isfriend="0" isfamily="0"/>
    <urls>
    <url type="photopage">http://www.flickr.com/photos/goukely/687589192/</url>
    </urls>
    </photo> */

}
