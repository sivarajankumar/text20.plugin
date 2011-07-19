/*
 * TestSimpleBrainTracker.java
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
package de.dfki.km.text20.sandbox.misc;

import java.net.URISyntaxException;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.uri.ClassURI;
import de.dfki.km.text20.services.evaluators.brain.BrainEvaluator;
import de.dfki.km.text20.services.evaluators.brain.BrainEvaluatorManager;
import de.dfki.km.text20.services.evaluators.brain.listenertypes.raw.RawBrainEvent;
import de.dfki.km.text20.services.evaluators.brain.listenertypes.raw.RawBrainListener;
import de.dfki.km.text20.services.trackingdevices.brain.BrainTrackingDevice;
import de.dfki.km.text20.services.trackingdevices.brain.BrainTrackingDeviceProvider;
import de.dfki.km.text20.services.trackingdevices.brain.BrainTrackingEvent;

/**
 * @author Ralf Biedert
 *
 */
public class TestSimpleBrainTracker {
    /**
     * @param args
     * @throws URISyntaxException
     * @throws InterruptedException 
     */
    public static void main(final String[] args) throws URISyntaxException,
                                                InterruptedException {

        // Load plugins, get a device, and get a evaluator
        final PluginManager pluginManager = PluginManagerFactory.createPluginManagerX().addPluginsFrom(ClassURI.CLASSPATH);
        final BrainTrackingDevice openDevice = pluginManager.getPlugin(BrainTrackingDeviceProvider.class).openDevice("discover://nearest");
        final BrainEvaluator evaluator = pluginManager.getPlugin(BrainEvaluatorManager.class).createEvaluator(openDevice);
        
        
        evaluator.addEvaluationListener(new RawBrainListener() {
            @Override
            public void newEvaluationEvent(RawBrainEvent event) {
                BrainTrackingEvent trackingEvent = event.getTrackingEvent();
                System.out.println(trackingEvent);
            }
            
            @Override
            public boolean requireUnfilteredEvents() {
                return true;
            }
        });

        System.out.println("Connected >!");

        Thread.sleep(100000);

    }
}
