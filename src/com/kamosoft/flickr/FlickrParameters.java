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

import java.io.Serializable;

/**
 * Main class which contains the app parameters.
 * @author Tom
 * created 23 mars 2011
 */
public class FlickrParameters
    implements Serializable
{
    private String appName;

    private String apiKey;

    private String apiSecret;

    private String authUrl;

    private String fullToken;

    private String perms;

    private String nsid;

    private String userName;

    private String realName;

    private String displayName;

    public FlickrParameters( String appName )
    {
        this.appName = appName;
    }

    /**
     * @return the perms
     */
    public String getPerms()
    {
        return perms;
    }

    /**
     * @param perms the perms to set
     */
    public void setPerms( String perms )
    {
        this.perms = perms;
    }

    /**
     * @return the nsid
     */
    public String getNsid()
    {
        return nsid;
    }

    /**
     * @param nsid the nsid to set
     */
    public void setNsid( String nsid )
    {
        this.nsid = nsid;
    }

    /**
     * @return the userName
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName( String userName )
    {
        this.userName = userName;
    }

    /**
     * @return the realName
     */
    public String getRealName()
    {
        return realName;
    }

    /**
     * @param realName the realName to set
     */
    public void setRealName( String realName )
    {
        this.realName = realName;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName( String displayName )
    {
        this.displayName = displayName;
    }

    /**
     * @param appName the appName to set
     */
    public void setAppName( String appName )
    {
        this.appName = appName;
    }

    /**
     * @param apiKey the apiKey to set
     */
    public void setApiKey( String apiKey )
    {
        this.apiKey = apiKey;
    }

    /**
     * @param apiSecret the apiSecret to set
     */
    public void setApiSecret( String apiSecret )
    {
        this.apiSecret = apiSecret;
    }

    /**
     * @param authUrl the authUrl to set
     */
    public void setAuthUrl( String authUrl )
    {
        this.authUrl = authUrl;
    }

    /**
     * @param fullToken the fullToken to set
     */
    public void setFullToken( String fullToken )
    {
        this.fullToken = fullToken;
    }

    /**
     * @return the appName
     */
    public String getAppName()
    {
        return appName;
    }

    /**
     * @return the apiKey
     */
    public String getApiKey()
    {
        return apiKey;
    }

    /**
     * @return the apiSecret
     */
    public String getApiSecret()
    {
        return apiSecret;
    }

    /**
     * @return the authUrl
     */
    public String getAuthUrl()
    {
        return authUrl;
    }

    /**
     * @return the fullToken
     */
    public String getFullToken()
    {
        return fullToken;
    }
}
