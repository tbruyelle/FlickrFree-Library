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

import java.util.Collection;

/**
 * Model object for Items element
 * @author tom
 */
public class Items
{
    private Collection<Item> item;

    private int page;

    private int pages;

    private int perpage;

    private int total;

    /**
     * @return the item
     */
    public Collection<Item> getItem()
    {
        return item;
    }

    /**
     * @return the page
     */
    public int getPage()
    {
        return page;
    }

    /**
     * @return the pages
     */
    public int getPages()
    {
        return pages;
    }

    /**
     * @return the perpage
     */
    public int getPerpage()
    {
        return perpage;
    }

    /**
     * @return the total
     */
    public int getTotal()
    {
        return total;
    }
}
