/*
 * PageManagerManagerImpl.java
 *
 * Copyright (c) 2010, Ralf Biedert, DFKI. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *
 */
package de.dfki.km.text20.browserplugin.services.pagemanager.impl;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import de.dfki.km.text20.browserplugin.services.pagemanager.PageManager;
import de.dfki.km.text20.browserplugin.services.pagemanager.PageManagerManager;
import de.dfki.km.text20.services.pseudorenderer.Pseudorenderer;

/**
 * @author Ralf Biedert
 */
@PluginImplementation
public class PageManagerManagerImpl implements PageManagerManager {

    /** */
    @InjectPlugin
    public PluginManager pluginManager;

    /* (non-Javadoc)
     * @see de.dfki.km.text20.browserplugin.services.pagemanager.PageManagerManager#createPageManager(de.dfki.km.text20.services.pseudorenderer.Pseudorenderer)
     */
    @Override
    public PageManager createPageManager(final Pseudorenderer pseudorenderer) {
        return new PageManagerImpl(this.pluginManager, pseudorenderer);
    }
}
