/*
 * TrackingListener.java
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
package de.dfki.km.text20.browserplugin.services.mastergazehandler.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import net.xeoh.plugins.base.PluginManager;
import de.dfki.km.text20.browserplugin.browser.browserplugin.JSExecutor;
import de.dfki.km.text20.browserplugin.services.mastergazehandler.MasterGazeHandler;
import de.dfki.km.text20.browserplugin.services.mastergazehandler.impl.gazehandler.AbstractGazeHandler;
import de.dfki.km.text20.browserplugin.services.mastergazehandler.impl.gazehandler.fixation.FixationHandler;
import de.dfki.km.text20.browserplugin.services.mastergazehandler.impl.gazehandler.reading.PerusalHandler;
import de.dfki.km.text20.browserplugin.services.mastergazehandler.impl.gazehandler.saccade.WeakSaccadeHandler;
import de.dfki.km.text20.browserplugin.services.mastergazehandler.impl.gazehandler.simple.HeadPositionHandler;
import de.dfki.km.text20.browserplugin.services.mastergazehandler.impl.gazehandler.simple.RawApplicationGazeHandler;
import de.dfki.km.text20.browserplugin.services.mastergazehandler.impl.gazehandler.simple.RawGazeHandler;
import de.dfki.km.text20.browserplugin.services.mastergazehandler.impl.gazehandler.simple.ReducedApplicationGazeHandler;
import de.dfki.km.text20.services.evaluators.gaze.GazeEvaluator;
import de.dfki.km.text20.services.evaluators.gaze.GazeEvaluatorManager;
import de.dfki.km.text20.services.pseudorenderer.Pseudorenderer;
import de.dfki.km.text20.services.trackingdevices.eyes.EyeTrackingDevice;
import de.dfki.km.text20.services.trackingdevices.eyes.EyeTrackingEvent;
import de.dfki.km.text20.services.trackingdevices.eyes.EyeTrackingListener;
import de.dfki.km.text20.util.filter.ChainedFilter;
import de.dfki.km.text20.util.filter.centralpoint.CentralPointFilter;
import de.dfki.km.text20.util.filter.centralpoint.VirtualMedianFilter;
import de.dfki.km.text20.util.filter.displacement.ReferenceBasedDisplacementFilter;

/**
 * Listens to tracking events.
 * 
 * @author rb
 *
 */
public class MasterGazeHandlerImpl implements MasterGazeHandler {

    /** */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /** */
    private final JSExecutor browserPlugin;

    /** List of registered handler */
    private final Map<String, List<String>> callbackHandler = new HashMap<String, List<String>>();

    /** */
    private GazeEvaluator evaluator;

    /** */
    private final PluginManager pluginManager;

    /** */
    private final Pseudorenderer pseudorenderer;

    /** */
    final List<AbstractGazeHandler> allGazeHandler = new ArrayList<AbstractGazeHandler>();

    /** Displace data */
    ReferenceBasedDisplacementFilter _calibrationFilter = new ReferenceBasedDisplacementFilter();

    /** Global filter to use */
    ChainedFilter filter = new ChainedFilter();

    /** */
    final AtomicLong lastObservedTime = new AtomicLong();

    /** Smooth incoming data */
    CentralPointFilter smoothingFilter = new VirtualMedianFilter(6);

    /** When to switch off reduced mode again */
    final AtomicLong switchReducedOffAt = new AtomicLong(Long.MAX_VALUE);

    // AbstractFilter filter = new SpakovFilter(5, 8, 12);
    // AbstractFilter filter = new FixedSmoothingFilter(20);
    // AbstractFilter filter = new EmptyFilter();

    /**
    * @param pluginManager   
    * @param browserPlugin 
    * @param pseudorenderer 
    */
    public MasterGazeHandlerImpl(final PluginManager pluginManager,
                                 final JSExecutor browserPlugin,
                                 final Pseudorenderer pseudorenderer) {
        this.browserPlugin = browserPlugin;
        this.pseudorenderer = pseudorenderer;
        this.pluginManager = pluginManager;
    }

    /**
     * @return the browserPlugin
     */
    public JSExecutor getBrowserPlugin() {
        return this.browserPlugin;
    }

    /**
     * Returns the corresponding evaluator
     * 
     * @return .
     */
    public GazeEvaluator getGazeEvaluator() {
        return this.evaluator;
    }

    /**
     * Return all registered handler of a given type.
     * 
     * @param type
     * @return .
     */
    public List<String> getHandlerForType(final String type) {
        if (!this.callbackHandler.containsKey(type)) {
            this.callbackHandler.put(type, new ArrayList<String>());
        }
        return this.callbackHandler.get(type);
    }

    /* (non-Javadoc)
     * @see de.dfki.km.augmentedtext.browserplugin.services.mastergazehandler.MasterGazeHandler#reduceJSLoad(int)
     */
    public void reduceJSLoad(final int timeToDisable) {
        for (final AbstractGazeHandler gazeHandler : this.allGazeHandler) {
            gazeHandler.setReducedCommunication(true);
        }

        final long l = this.lastObservedTime.get();

        this.switchReducedOffAt.set(l + timeToDisable);
    }

    /* (non-Javadoc)
     * @see de.dfki.km.augmentedtext.browserplugin.services.mastergazehandler.impl.MasterGazeHandler#registerJSCallback(java.lang.String, java.lang.String)
     */
    public void registerJSCallback(final String type, final String listener) {

        // Make sure we have something for the type
        if (!this.callbackHandler.containsKey(type)) {
            this.callbackHandler.put(type, new ArrayList<String>());
        }

        final List<String> handler = this.callbackHandler.get(type);
        handler.add(listener);
    }

    /* (non-Javadoc)
     * @see de.dfki.km.augmentedtext.browserplugin.services.mastergazehandler.impl.MasterGazeHandler#removeJSCallback(java.lang.String)
     */
    public void removeJSCallback(final String listener) {
        for (final String type : this.callbackHandler.keySet()) {
            final List<String> all = this.callbackHandler.get(type);
            if (all == null) {
                continue;
            }

            if (all.contains(listener)) {
                all.remove(listener);
            }
        }
    }

    /* (non-Javadoc)
     * @see de.dfki.km.augmentedtext.browserplugin.services.mastergazehandler.MasterGazeHandler#setTrackingDevice(de.dfki.km.augmentedtext.services.trackingdevices.TrackingDevice)
     */
    public void setTrackingDevice(final EyeTrackingDevice device) {
        init(device);
    }

    /**
     * Called after the tracking device has been set.
     */
    private void init(final EyeTrackingDevice trackingDevice) {
        this.logger.fine("Starting MasterGazeHandler.");

        // Setup filter
        this.filter.addFilter(this.smoothingFilter);

        // Create gaze evaluator 
        final GazeEvaluatorManager evaluationManager = this.pluginManager.getPlugin(GazeEvaluatorManager.class);

        this.evaluator = evaluationManager.createGazeEvaluator(trackingDevice);
        this.evaluator.setGazeFilter(this.filter);

        // Create gaze handler
        this.allGazeHandler.add(new RawGazeHandler());
        this.allGazeHandler.add(new RawApplicationGazeHandler());
        this.allGazeHandler.add(new FixationHandler());
        this.allGazeHandler.add(new ReducedApplicationGazeHandler());
        this.allGazeHandler.add(new PerusalHandler());
        this.allGazeHandler.add(new HeadPositionHandler());
        this.allGazeHandler.add(new WeakSaccadeHandler());

        for (final AbstractGazeHandler gazeHandler : this.allGazeHandler) {
            gazeHandler.init(this, this.pseudorenderer, this.browserPlugin, this.evaluator);
        }

        trackingDevice.addTrackingListener(new EyeTrackingListener() {

            public void newTrackingEvent(final EyeTrackingEvent event) {
                MasterGazeHandlerImpl.this.lastObservedTime.set(event.getEventTime());

                // If we're past switchoff time
                if (MasterGazeHandlerImpl.this.lastObservedTime.get() > MasterGazeHandlerImpl.this.switchReducedOffAt.get()) {
                    MasterGazeHandlerImpl.this.switchReducedOffAt.set(Long.MAX_VALUE);

                    // Switch off reduced mode
                    for (final AbstractGazeHandler gazeHandler : MasterGazeHandlerImpl.this.allGazeHandler) {
                        gazeHandler.setReducedCommunication(false);
                    }
                }
            }
        });
    }

    /**
     * @return the filter
     */
    public ChainedFilter getMasterFilterChain() {
        return this.filter;
    }
}