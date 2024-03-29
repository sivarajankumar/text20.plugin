/*
 * PerusalHandlerImpl3.java
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
package de.dfki.km.text20.services.evaluators.gaze.impl.handler.perusal.v4;

import java.awt.Rectangle;
import java.util.Collection;

import de.dfki.km.text20.services.evaluators.gaze.listenertypes.perusal.PerusalEvent;
import de.dfki.km.text20.services.evaluators.gaze.listenertypes.perusal.PerusalListener;
import de.dfki.km.text20.services.evaluators.gaze.listenertypes.saccade.SaccadeEvent;
import de.dfki.km.text20.services.evaluators.gaze.listenertypes.saccade.SaccadeListener;
import de.dfki.km.text20.services.evaluators.gaze.util.handler.AbstractGazeHandler;
import de.dfki.km.text20.services.pseudorenderer.Pseudorenderer;
import de.dfki.km.text20.services.pseudorenderer.RenderElement;
import de.dfki.km.text20.services.pseudorenderer.util.elements.TextualRenderElementCharPositions;

/**
 * Detects perusal progress.
 * 
 * TODO: Replace this with latest version from experimental branch.
 * 
 * @author Ralf Biedert
 */
public class PerusalHandlerImpl4 extends
        AbstractGazeHandler<PerusalEvent, PerusalListener> {

    /** Psedorenderer to use */
    Pseudorenderer pseudorenderer;

    /** */
    long currentTime;

    /** */
    TextualRenderElementCharPositions tecp = new TextualRenderElementCharPositions();

    
    /* (non-Javadoc)
     * @see de.dfki.km.text20.services.evaluators.gaze.util.handler.AbstractGazeHandler#init()
     */
    @Override
    public void init() {

        this.pseudorenderer = this.attachedListener.getPseudorenderer();
        this.gazeEvaluator.addEvaluationListener(new SaccadeListener() {
            @Override
            public void newEvaluationEvent(SaccadeEvent event) {
                          //
                      }
        });
    }

    /**
     * Dispatches the new event to the listener.
     */
    @SuppressWarnings("unused")
    private void dispatch(final float speed, final Rectangle rectangle,
                          final Object TODO,
                          final Collection<RenderElement> elementsForDocumentArea) {

        // 'Exports'
        final long time = this.currentTime;

        callListener(new PerusalEvent() {
            @Override
            public long getGenerationTime() {
                // TODO Auto-generated method stub
                return 0;
            }
        });
    }
}
